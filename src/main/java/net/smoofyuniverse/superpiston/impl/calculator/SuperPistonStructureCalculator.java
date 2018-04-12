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

package net.smoofyuniverse.superpiston.impl.calculator;

import com.flowpowered.math.vector.Vector3i;
import net.smoofyuniverse.superpiston.api.structure.calculator.DefaultStructureCalculator;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;

public class SuperPistonStructureCalculator extends DefaultStructureCalculator {

	public SuperPistonStructureCalculator(World world, BlockSnapshot piston, Direction direction, Direction movement) {
		super(world, piston, direction, movement);
		this.maxBlocks = 100;
	}

	@Override
	public boolean isSticky(BlockState state, Vector3i pos) {
		return super.isSticky(state, pos) || state.getType() == BlockTypes.WEB;
	}

	@Override
	public MovementReaction getReaction(BlockState state, Vector3i pos) {
		if (state.getType() == BlockTypes.OBSIDIAN || state.getType() == BlockTypes.WEB)
			return MovementReaction.NORMAL;
		return super.getReaction(state, pos);
	}
}
