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

package net.smoofyuniverse.superpiston.mixin;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.smoofyuniverse.superpiston.SuperPiston;
import net.smoofyuniverse.superpiston.SuperPistonTimings;
import net.smoofyuniverse.superpiston.api.structure.PistonStructure;
import net.smoofyuniverse.superpiston.api.structure.calculator.DefaultStructureCalculator;
import net.smoofyuniverse.superpiston.api.structure.calculator.PistonStructureCalculator;
import net.smoofyuniverse.superpiston.impl.event.PostStructureCalculationEvent;
import net.smoofyuniverse.superpiston.impl.event.PreStructureCalculationEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.data.util.DirectionResolver;
import org.spongepowered.common.util.VecHelper;

import java.util.List;

@Mixin(BlockPistonStructureHelper.class)
public class MixinBlockPistonStructureHelper {
	@Shadow
	@Final
	private World world;
	@Shadow
	@Final
	private List<BlockPos> toMove;
	@Shadow
	@Final
	private List<BlockPos> toDestroy;

	private Direction direction, movement;
	private BlockSnapshot piston;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(World world, BlockPos pos, EnumFacing pistonFacing, boolean extending, CallbackInfo ci) {
		this.piston = ((org.spongepowered.api.world.World) this.world).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
		this.direction = DirectionResolver.getFor(pistonFacing);
		this.movement = extending ? this.direction : this.direction.getOpposite();
	}

	/**
	 * @author Yeregorix
	 */
	@Overwrite
	public boolean canMove() {
		SuperPistonTimings.CALCULATION.startTiming();
		boolean r = calculate();
		SuperPistonTimings.CALCULATION.stopTiming();

		return r;
	}

	public boolean calculate() {
		this.toMove.clear();
		this.toDestroy.clear();

		Cause cause = Sponge.getCauseStackManager().getCurrentCause();

		PreStructureCalculationEvent preEvent = new PreStructureCalculationEvent(
				cause, (org.spongepowered.api.world.World) this.world, this.piston, this.direction, this.movement,
				new DefaultStructureCalculator((org.spongepowered.api.world.World) this.world, this.piston, this.direction, this.movement));

		Sponge.getEventManager().post(preEvent);

		if (preEvent.isCancelled())
			return false;

		PistonStructureCalculator calculator = preEvent.getCalculator();
		PistonStructure structure = null;
		try {
			structure = calculator.calculateStructure();
		} catch (Exception e) {
			SuperPiston.LOGGER.error("Unexpected exception has occurred during structure calculation", e);
		}

		if (structure == null)
			return false;

		PostStructureCalculationEvent postEvent = new PostStructureCalculationEvent(
				cause, (org.spongepowered.api.world.World) this.world, this.piston, this.direction, this.movement,
				calculator, structure);

		Sponge.getEventManager().post(postEvent);

		if (postEvent.isCancelled())
			return false;

		for (Vector3i pos : structure.getBlocksToMove())
			this.toMove.add(VecHelper.toBlockPos(pos));

		for (Vector3i pos : structure.getBlocksToDestroy())
			this.toDestroy.add(VecHelper.toBlockPos(pos));

		return structure.isMoveable();
	}
}
