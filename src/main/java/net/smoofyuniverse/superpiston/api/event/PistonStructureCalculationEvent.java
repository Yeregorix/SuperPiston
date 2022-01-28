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

package net.smoofyuniverse.superpiston.api.event;

import net.smoofyuniverse.superpiston.api.structure.PistonStructure;
import net.smoofyuniverse.superpiston.api.structure.calculator.PistonStructureCalculator;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.server.ServerWorld;

/**
 * Base event for when a piston determines a {@link PistonStructure}.
 */
public interface PistonStructureCalculationEvent extends Event, Cancellable {

	/**
	 * Gets the world.
	 *
	 * @return The world.
	 */
	ServerWorld world();

	/**
	 * Gets the block representing the piston.
	 * In a modded environment this block might not even be a real piston.
	 * Do not use this block to determine the direction of the piston.
	 * Use {@link PistonStructureCalculationEvent#pistonDirection()} instead.
	 *
	 * @return The block representing the piston.
	 */
	BlockSnapshot piston();

	/**
	 * @return The direction of the piston.
	 */
	Direction pistonDirection();

	/**
	 * Gets the direction of the movement.
	 * If it matches {@link PistonStructureCalculationEvent#pistonDirection()} then the piston is extending.
	 * If it matches the opposite then the piston is retracting.
	 * If it matches something else then a mod is doing weird stuff.
	 *
	 * @return The direction of the movement.
	 */
	Direction pistonMovement();

	/**
	 * Called before the {@link PistonStructure} is calculated.
	 * This event is used to decide which {@link PistonStructureCalculator} will calculate the structure.
	 */
	public interface Pre extends PistonStructureCalculationEvent {

		/**
		 * @return The original calculator.
		 */
		PistonStructureCalculator originalCalculator();

		/**
		 * @return The calculator that will be used.
		 */
		PistonStructureCalculator calculator();

		/**
		 * Sets the calculator the will be used.
		 *
		 * @param value The calculator.
		 */
		void setCalculator(PistonStructureCalculator value);
	}

	/**
	 * Called after the {@link PistonStructure} is calculated.
	 */
	public interface Post extends PistonStructureCalculationEvent {

		/**
		 * @return The calculator that has been used.
		 */
		PistonStructureCalculator calculator();

		/**
		 * @return The calculated structure.
		 */
		PistonStructure structure();
	}
}
