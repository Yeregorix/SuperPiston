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

package net.smoofyuniverse.superpiston.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.smoofyuniverse.superpiston.SuperPiston;
import net.smoofyuniverse.superpiston.api.structure.PistonStructure;
import net.smoofyuniverse.superpiston.api.structure.calculator.DefaultStructureCalculator;
import net.smoofyuniverse.superpiston.api.structure.calculator.PistonStructureCalculator;
import net.smoofyuniverse.superpiston.impl.event.PostStructureCalculationEvent;
import net.smoofyuniverse.superpiston.impl.event.PreStructureCalculationEvent;
import net.smoofyuniverse.superpiston.impl.internal.InternalStructureResolver;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(PistonStructureResolver.class)
public abstract class PistonStructureResolverMixin implements InternalStructureResolver {
	@Final
	@Shadow
	private Level level;
	@Mutable
	@Final
	@Shadow
	private List<BlockPos> toPush;
	@Mutable
	@Final
	@Shadow
	private List<BlockPos> toDestroy;
	@Final
	@Shadow
	private net.minecraft.core.Direction pushDirection;

	private Direction direction, movement;
	private BlockSnapshot piston;
	private boolean resolveCustom = true;

	@Inject(method = "resolve", at = @At("HEAD"), cancellable = true)
	public void onResolve(CallbackInfoReturnable<Boolean> cir) {
		if (this.resolveCustom)
			cir.setReturnValue(resolveCustom());
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(Level level, BlockPos pos, net.minecraft.core.Direction pistonDirection, boolean extending, CallbackInfo ci) {
		this.piston = ((ServerWorld) this.level).createSnapshot(pos.getX(), pos.getY(), pos.getZ());
		this.direction = Constants.DirectionFunctions.getFor(pistonDirection);
		this.movement = extending ? this.direction : this.direction.opposite();
	}

	private boolean resolveCustom() {
		this.toPush.clear();
		this.toDestroy.clear();

		Cause cause = Sponge.server().causeStackManager().currentCause();

		PreStructureCalculationEvent preEvent = new PreStructureCalculationEvent(
				cause, (ServerWorld) this.level, this.piston, this.direction, this.movement,
				new DefaultStructureCalculator((ServerWorld) this.level, this.piston, this.direction, this.movement));

		Sponge.eventManager().post(preEvent);

		if (preEvent.isCancelled())
			return false;

		PistonStructureCalculator calculator = preEvent.calculator();
		PistonStructure structure = null;
		try {
			structure = calculator.calculateStructure();
		} catch (Exception e) {
			SuperPiston.LOGGER.error("Unexpected exception has occurred during structure calculation", e);
		}

		if (structure == null)
			return false;

		PostStructureCalculationEvent postEvent = new PostStructureCalculationEvent(
				cause, (ServerWorld) this.level, this.piston, this.direction, this.movement,
				calculator, structure);

		Sponge.eventManager().post(postEvent);

		if (postEvent.isCancelled())
			return false;

		for (Vector3i pos : structure.getBlocksToMove())
			this.toPush.add(VecHelper.toBlockPos(pos));

		for (Vector3i pos : structure.getBlocksToDestroy())
			this.toDestroy.add(VecHelper.toBlockPos(pos));

		return structure.isMoveable();
	}

	private void resolveVanilla() {
		this.resolveCustom = false;
		resolve();
		this.resolveCustom = true;
	}

	@Shadow
	public abstract boolean resolve();

	@Override
	public Set<BlockPos> resolveBlocksToRefresh() {
		Set<BlockPos> toRefresh = new HashSet<>();

		// Backup custom blocks
		List<BlockPos> toPushCustom = this.toPush;
		List<BlockPos> toDestroyCustom = this.toDestroy;
		this.toPush = new ArrayList<>();
		this.toDestroy = new ArrayList<>();

		// Resolve vanilla blocks
		resolveVanilla();

		// Difference between blocks updated by the client and blocks updated by the server
		for (BlockPos pos : this.toPush) {
			toRefresh.add(pos);
			toRefresh.add(pos.relative(this.pushDirection));
		}
		toRefresh.addAll(this.toDestroy);
		for (BlockPos pos : toPushCustom) {
			toRefresh.remove(pos);
			toRefresh.remove(pos.relative(this.pushDirection));
		}
		for (BlockPos pos : toDestroyCustom) {
			toRefresh.remove(pos);
		}

		// Restore custom blocks
		this.toPush = toPushCustom;
		this.toDestroy = toDestroyCustom;

		return toRefresh;
	}
}
