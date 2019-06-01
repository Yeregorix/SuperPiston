/*
 * Copyright (c) 2018-2019 Hugo Dupanloup (Yeregorix)
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

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.smoofyuniverse.superpiston.api.structure.calculator.DefaultStructureCalculator.MovementReaction;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;
import org.spongepowered.common.util.VecHelper;

public class ReactionUtil {

	public static MovementReaction getDefaultReaction(World world, BlockState state, Vector3i pos, Direction movement) {
		BlockPos blockPos = VecHelper.toBlockPos(pos);
		net.minecraft.world.World nmsWorld = (net.minecraft.world.World) world;

		if (!nmsWorld.getWorldBorder().contains(blockPos))
			return MovementReaction.BLOCK;

		if (pos.getY() < 0 || (movement == Direction.DOWN && pos.getY() == 0))
			return MovementReaction.BLOCK;

		int h = nmsWorld.getHeight() - 1;
		if (pos.getY() > h || (movement == Direction.UP && pos.getY() == h))
			return MovementReaction.BLOCK;

		if (BlockUtil.hasTileEntity(state))
			return MovementReaction.BLOCK;

		IBlockState nmsState = (IBlockState) state;
		Block block = nmsState.getBlock();

		if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON)
			return nmsState.getValue(BlockPistonBase.EXTENDED) ? MovementReaction.BLOCK : MovementReaction.NORMAL;

		if (block == Blocks.OBSIDIAN || nmsState.getBlockHardness(nmsWorld, blockPos) == -1.0f)
			return MovementReaction.BLOCK;

		return fromNMS(nmsState.getPushReaction());
	}

	public static MovementReaction fromNMS(EnumPushReaction reaction) {
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
