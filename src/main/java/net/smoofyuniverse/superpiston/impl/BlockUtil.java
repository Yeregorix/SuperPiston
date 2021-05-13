/*
 * Copyright (c) 2018-2021 Hugo Dupanloup (Yeregorix)
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
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.World;
import org.spongepowered.common.util.VecHelper;

public class BlockUtil {
	private static final boolean useForge = detectForge();

	@SuppressWarnings("deprecation")
	public static boolean hasTileEntity(BlockState state) {
		IBlockState nmsState = (IBlockState) state;
		return useForge ? nmsState.getBlock().hasTileEntity(nmsState) : nmsState.getBlock().hasTileEntity();
	}

	public static boolean isAir(World world, BlockState state, Vector3i pos) {
		IBlockState nmsState = (IBlockState) state;
		return useForge ? nmsState.getBlock().isAir(nmsState, (IBlockAccess) world, VecHelper.toBlockPos(pos)) : nmsState.getMaterial() == Material.AIR;
	}

	private static boolean detectForge() {
		try {
			Class.forName("net.minecraftforge.common.ForgeVersion");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
