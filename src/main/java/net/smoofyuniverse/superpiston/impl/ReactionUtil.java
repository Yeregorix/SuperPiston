/*
 * Copyright (c) 2018-2022 Hugo Dupanloup (Yeregorix)
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
import net.minecraft.world.level.material.PushReaction;
import net.smoofyuniverse.superpiston.api.structure.calculator.DefaultStructureCalculator.MovementReaction;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

public class ReactionUtil {

	public static MovementReaction getDefaultReaction(ServerWorld world, org.spongepowered.api.block.BlockState state, Vector3i pos, Direction movement) {
		BlockPos blockPos = VecHelper.toBlockPos(pos);
		ServerLevel level = (ServerLevel) world;

		if (!level.getWorldBorder().isWithinBounds(blockPos))
			return MovementReaction.BLOCK;

		if (pos.y() < 0 || (movement == Direction.DOWN && pos.y() == 0))
			return MovementReaction.BLOCK;

		int h = level.getMaxBuildHeight() - 1;
		if (pos.y() > h || (movement == Direction.UP && pos.y() == h))
			return MovementReaction.BLOCK;

		if (BlockUtil.isEntityBlock(state))
			return MovementReaction.BLOCK;

		BlockState nmsState = (BlockState) state;
		Block block = nmsState.getBlock();
		if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON)
			return nmsState.getValue(PistonBaseBlock.EXTENDED) ? MovementReaction.BLOCK : MovementReaction.NORMAL;

		if (block == Blocks.OBSIDIAN || nmsState.getDestroySpeed(level, blockPos) == -1.0f)
			return MovementReaction.BLOCK;

		return fromNMS(nmsState.getPistonPushReaction());
	}

	public static MovementReaction fromNMS(PushReaction reaction) {
		switch (reaction) {
			case NORMAL:
				return MovementReaction.NORMAL;
			case DESTROY:
				return MovementReaction.DESTROY;
			case BLOCK:
				return MovementReaction.BLOCK;
			case PUSH_ONLY:
				return MovementReaction.PUSH_ONLY;
			case IGNORE:
				throw new IllegalArgumentException("IGNORE");
			default:
				throw new IllegalArgumentException();
		}
	}
}
