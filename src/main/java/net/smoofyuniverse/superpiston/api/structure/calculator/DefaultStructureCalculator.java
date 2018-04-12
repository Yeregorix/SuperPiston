/*
 * Copyright (c) 2018 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.smoofyuniverse.superpiston.api.structure.calculator;

import com.flowpowered.math.vector.Vector3i;
import net.smoofyuniverse.superpiston.api.structure.PistonStructure;
import net.smoofyuniverse.superpiston.impl.BlockUtil;
import net.smoofyuniverse.superpiston.impl.ReactionUtil;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultStructureCalculator implements PistonStructureCalculator {
	private static final Direction[] CARDINALS = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

	protected final World world;
	protected final BlockSnapshot piston;
	protected final Direction direction, movement;

	protected final Direction[] sides;
	protected final boolean extending;

	protected List<Vector3i> toMove, toDestroy;
	protected Set<Vector3i> toMoveSet;
	protected int maxBlocks = 12;

	public DefaultStructureCalculator(World world, BlockSnapshot piston, Direction direction, Direction movement) {
		if (direction != movement && direction != movement.getOpposite())
			throw new IllegalArgumentException("movement");

		this.world = world;
		this.piston = piston;
		this.direction = direction;
		this.movement = movement;

		this.sides = new Direction[4];
		int i = 0;
		for (Direction dir : CARDINALS) {
			if (dir == movement)
				continue;
			if (dir == movement.getOpposite())
				continue;
			this.sides[i] = dir;
			i++;
		}

		this.extending = direction == movement;
	}

	@Override
	public PistonStructure calculateStructure() {
		this.toMove = new ArrayList<>(this.maxBlocks);
		this.toDestroy = new ArrayList<>();
		this.toMoveSet = new HashSet<>();

		Vector3i offset = this.direction.asBlockOffset();
		boolean moveable = calculate(this.piston.getPosition().add(this.extending ? offset : offset.mul(2)));

		PistonStructure structure = moveable ? new PistonStructure(this.toMove, this.toDestroy) : new PistonStructure(false);
		this.toMove = null;
		this.toDestroy = null;
		this.toMoveSet = null;
		return structure;
	}

	@SuppressWarnings("ForLoopReplaceableByForEach")
	protected boolean calculate(Vector3i origin) {
		BlockState state = this.world.getBlock(origin);

		MovementReaction reaction = getReaction(state, origin);
		if (reaction == MovementReaction.DESTROY) {
			this.toDestroy.add(origin);
			return true;
		}
		if (reaction == MovementReaction.BLOCK)
			return false;

		if (!addBlockLine(origin, this.movement))
			return false;

		for (int i = 0; i < this.toMove.size(); ++i) {
			Vector3i pos = this.toMove.get(i);
			if (isSticky(this.world.getBlock(pos), pos) && !addBranchingBlocks(pos))
				return false;
		}

		return true;
	}

	protected boolean addBlockLine(Vector3i origin, Direction dir) {
		BlockState state = this.world.getBlock(origin);

		if (BlockUtil.isAir(this.world, state, origin))
			return true;

		if (this.toMoveSet.contains(origin))
			return true;

		if (this.piston.getPosition().equals(origin))
			return true;

		MovementReaction reaction = getReaction(state, origin);
		if (reaction != MovementReaction.NORMAL && (reaction != MovementReaction.PUSH_ONLY || this.movement != dir))
			return true;

		int i = 1;
		if (i + this.toMoveSet.size() > this.maxBlocks)
			return false;

		Vector3i offset = this.movement.asBlockOffset();

		Vector3i pos = origin;
		while (isSticky(state, pos)) {
			pos = pos.sub(offset);
			state = this.world.getBlock(pos);

			if (BlockUtil.isAir(this.world, state, pos))
				break;

			if (this.piston.getPosition().equals(pos))
				break;

			if (getReaction(state, pos) != MovementReaction.NORMAL)
				break;

			i++;
			if (i + this.toMoveSet.size() > this.maxBlocks)
				return false;
		}

		int m = 0;
		for (int j = i - 1; j >= 0; --j) {
			pos = origin.sub(offset.mul(j));

			this.toMove.add(pos);
			this.toMoveSet.add(pos);
			m++;
		}

		pos = origin;
		while (true) {
			pos = pos.add(offset);

			int k = this.toMove.indexOf(pos);
			if (k > -1) {
				reorderListAtCollision(m, k);

				for (int l = 0; l <= k + m; ++l) {
					Vector3i pos2 = this.toMove.get(l);
					if (isSticky(this.world.getBlock(pos2), pos2) && !addBranchingBlocks(pos2))
						return false;
				}

				return true;
			}

			state = this.world.getBlock(pos);

			if (BlockUtil.isAir(this.world, state, pos))
				return true;

			if (this.piston.getPosition().equals(pos))
				return false;

			reaction = getReaction(state, pos);
			if (reaction == MovementReaction.BLOCK)
				return false;

			if (reaction == MovementReaction.DESTROY) {
				this.toDestroy.add(pos);
				return true;
			}

			if (this.toMoveSet.size() >= this.maxBlocks)
				return false;

			this.toMove.add(pos);
			this.toMoveSet.add(pos);

			m++;
		}
	}

	protected boolean addBranchingBlocks(Vector3i origin) {
		for (Direction dir : this.sides) {
			if (!addBlockLine(origin.add(dir.asBlockOffset()), dir))
				return false;
		}
		return true;
	}

	protected void reorderListAtCollision(int a, int b) {
		int size = this.toMove.size();

		List<Vector3i> list = new ArrayList<>(size);
		list.addAll(this.toMove.subList(0, b));
		list.addAll(this.toMove.subList(size - a, size));
		list.addAll(this.toMove.subList(b, size - a));

		this.toMove.clear();
		this.toMove.addAll(list);
	}

	public boolean isSticky(BlockState state, Vector3i pos) {
		return state.getType() == BlockTypes.SLIME;
	}

	public MovementReaction getReaction(BlockState state, Vector3i pos) {
		return ReactionUtil.getDefaultReaction(this.world, state, pos, this.movement);
	}

	public static enum MovementReaction {
		NORMAL, DESTROY, BLOCK, PUSH_ONLY
	}
}
