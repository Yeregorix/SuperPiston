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

package net.smoofyuniverse.superpiston.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import net.smoofyuniverse.superpiston.api.structure.calculator.DefaultStructureCalculator.MovementReaction;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.block.BlockState;

import java.util.Map;

@ConfigSerializable
public class PistonConfig {
	public static final TypeToken<PistonConfig> TOKEN = TypeToken.of(PistonConfig.class);

	@Setting(value = "BlockReactions")
	public Map<BlockState, MovementReaction> blockReactions;
	@Setting(value = "StickyBlocks")
	public Map<BlockState, Boolean> stickyBlocks;
	@Setting(value = "MaxBlocks")
	public int maxBlocks = 12;

	public Immutable toImmutable() {
		return new Immutable(this.blockReactions, this.stickyBlocks, this.maxBlocks);
	}

	public static class Immutable {
		public final Map<BlockState, MovementReaction> blockReactions;
		public final Map<BlockState, Boolean> stickyBlocks;
		public final int maxBlocks;

		public Immutable(Map<BlockState, MovementReaction> blockReactions, Map<BlockState, Boolean> stickyBlocks, int maxBlocks) {
			this.blockReactions = ImmutableMap.copyOf(blockReactions);
			this.stickyBlocks = ImmutableMap.copyOf(stickyBlocks);
			this.maxBlocks = maxBlocks;
		}
	}
}
