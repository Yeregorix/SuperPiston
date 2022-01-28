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
import net.smoofyuniverse.superpiston.api.structure.calculator.PistonStructureCalculator;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.server.ServerWorld;

public class PreStructureCalculationEvent extends AbstractStructureCalculationEvent implements PistonStructureCalculationEvent.Pre {
	private final PistonStructureCalculator originalCalculator;
	private PistonStructureCalculator calculator;

	public PreStructureCalculationEvent(Cause cause, ServerWorld world, BlockSnapshot piston, Direction direction, Direction movement, PistonStructureCalculator calculator) {
		super(cause, world, piston, direction, movement);
		this.originalCalculator = calculator;
		this.calculator = calculator;
	}

	@Override
	public PistonStructureCalculator originalCalculator() {
		return this.originalCalculator;
	}

	@Override
	public PistonStructureCalculator calculator() {
		return this.calculator;
	}

	@Override
	public void setCalculator(PistonStructureCalculator value) {
		if (value == null)
			throw new IllegalArgumentException();
		this.calculator = value;
	}
}
