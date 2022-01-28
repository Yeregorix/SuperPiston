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

package net.smoofyuniverse.superpiston.impl.event;

import net.smoofyuniverse.superpiston.api.event.PistonStructureCalculationEvent;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.server.ServerWorld;

public abstract class AbstractStructureCalculationEvent extends AbstractEvent implements PistonStructureCalculationEvent {
	private final Cause cause;
	private final ServerWorld world;
	private final BlockSnapshot piston;
	private final Direction direction, movement;
	private boolean cancelled = false;

	public AbstractStructureCalculationEvent(Cause cause, ServerWorld world, BlockSnapshot piston, Direction direction, Direction movement) {
		this.cause = cause;
		this.world = world;
		this.piston = piston;
		this.direction = direction;
		this.movement = movement;
	}

	@Override
	public Cause cause() {
		return this.cause;
	}

	@Override
	public ServerWorld world() {
		return this.world;
	}

	@Override
	public BlockSnapshot piston() {
		return this.piston;
	}

	@Override
	public Direction pistonDirection() {
		return this.direction;
	}

	@Override
	public Direction pistonMovement() {
		return this.movement;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}
