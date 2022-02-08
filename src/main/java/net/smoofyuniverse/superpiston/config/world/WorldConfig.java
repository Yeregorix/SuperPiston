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

package net.smoofyuniverse.superpiston.config.world;

import com.google.common.collect.ImmutableMap;
import net.smoofyuniverse.superpiston.SuperPiston;
import net.smoofyuniverse.superpiston.api.structure.calculator.DefaultStructureCalculator.MovementReaction;
import net.smoofyuniverse.superpiston.util.IOUtil;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static net.smoofyuniverse.superpiston.util.RegistryUtil.resolveBlockStates;
import static org.spongepowered.math.GenericMath.clamp;

@ConfigSerializable
public class WorldConfig {
	public static final int CURRENT_VERSION = 1, MINIMUM__VERSION = 1;
	public static final Resolved VANILLA = new Resolved(ImmutableMap.of(), ImmutableMap.of(), 12);

	@Setting(value = "BlockReactions")
	public Map<String, MovementReaction> blockReactions = new HashMap<>();
	@Setting(value = "StickyBlocks")
	public Map<String, Boolean> stickyBlocks = new HashMap<>();
	@Setting(value = "MaxBlocks")
	public int maxBlocks = 12;

	public Resolved resolve() {
		return new Resolved(resolveBlockStates(this.blockReactions), resolveBlockStates(this.stickyBlocks), this.maxBlocks);
	}

	public static WorldConfig load(Path file) throws IOException {
		ConfigurationLoader<CommentedConfigurationNode> loader = SuperPiston.get().createConfigLoader(file);

		CommentedConfigurationNode root = loader.load();
		int version = root.node("Version").getInt();
		if ((version > CURRENT_VERSION || version < MINIMUM__VERSION) && IOUtil.backup(file).isPresent()) {
			SuperPiston.LOGGER.info("Your config version is not supported. A new one will be generated.");
			root = loader.createNode();
		}

		ConfigurationNode cfgNode = root.node("Config");
		WorldConfig cfg = cfgNode.get(WorldConfig.class, new WorldConfig());

		cfg.maxBlocks = clamp(cfg.maxBlocks, 1, 500);

		root.node("Version").set(CURRENT_VERSION);
		cfgNode.set(cfg);
		loader.save(root);
		return cfg;
	}

	public static class Resolved {
		public final Map<BlockState, MovementReaction> blockReactions;
		public final Map<BlockState, Boolean> stickyBlocks;
		public final int maxBlocks;

		public Resolved(Map<BlockState, MovementReaction> blockReactions, Map<BlockState, Boolean> stickyBlocks, int maxBlocks) {
			this.blockReactions = ImmutableMap.copyOf(blockReactions);
			this.stickyBlocks = ImmutableMap.copyOf(stickyBlocks);
			this.maxBlocks = maxBlocks;
		}
	}
}
