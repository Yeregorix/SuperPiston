/*
 * Copyright (c) 2018-2024 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.superpiston.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.smoofyuniverse.superpiston.api.structure.calculator.DefaultStructureCalculator.MovementReaction;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

public class ReactionUtil {

	public static boolean isPositionBlocked(ServerWorld world, Vector3i pos, Direction movement) {
		BlockPos blockPos = VecHelper.toBlockPos(pos);
		ServerLevel level = (ServerLevel) world;

		if (!level.getWorldBorder().isWithinBounds(blockPos))
			return true;

		if (pos.y() < 0 || (movement == Direction.DOWN && pos.y() == 0))
			return true;

		int h = level.getMaxBuildHeight() - 1;
		if (pos.y() > h || (movement == Direction.UP && pos.y() == h))
			return true;

		return false;
	}

	public static MovementReaction getDefaultReaction(org.spongepowered.api.block.BlockState state) {
		if (BlockUtil.hasBlockEntity(state))
			return MovementReaction.BLOCK;

		BlockState nmsState = (BlockState) state;
		Block block = nmsState.getBlock();
		if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON)
			return nmsState.getValue(PistonBaseBlock.EXTENDED) ? MovementReaction.BLOCK : MovementReaction.NORMAL;

		if (block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN
				|| block == Blocks.RESPAWN_ANCHOR || block == Blocks.REINFORCED_DEEPSLATE
				|| block.defaultDestroyTime() == -1.0f)
			return MovementReaction.BLOCK;

		return switch (nmsState.getPistonPushReaction()) {
			case NORMAL -> MovementReaction.NORMAL;
			case DESTROY -> MovementReaction.DESTROY;
			case BLOCK -> MovementReaction.BLOCK;
			case PUSH_ONLY -> MovementReaction.PUSH_ONLY;
			case IGNORE -> throw new IllegalArgumentException("IGNORE");
		};
	}
}
