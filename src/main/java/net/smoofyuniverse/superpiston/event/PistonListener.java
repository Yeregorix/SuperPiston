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

package net.smoofyuniverse.superpiston.event;

import net.smoofyuniverse.superpiston.SuperPiston;
import net.smoofyuniverse.superpiston.api.event.PistonStructureCalculationEvent;
import net.smoofyuniverse.superpiston.impl.calculator.SuperPistonStructureCalculator;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.world.server.ServerWorld;

public class PistonListener {
	private final SuperPiston plugin;

	public PistonListener(SuperPiston plugin) {
		this.plugin = plugin;
	}

	@Listener(order = Order.FIRST)
	public void onPreStructureCalculation(PistonStructureCalculationEvent.Pre e) {
		ServerWorld world = e.world();
		e.setCalculator(new SuperPistonStructureCalculator(world,
				e.piston(), e.pistonDirection(), e.pistonMovement(), this.plugin.getConfig(world)));
	}
}
