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

package net.smoofyuniverse.superpiston.config.world;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import net.smoofyuniverse.superpiston.SuperPiston;
import net.smoofyuniverse.superpiston.api.structure.calculator.DefaultStructureCalculator.MovementReaction;
import net.smoofyuniverse.superpiston.util.IOUtil;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.block.BlockState;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static net.smoofyuniverse.superpiston.util.MathUtil.clamp;

@ConfigSerializable
public class WorldConfig {
	public static final int CURRENT_VERSION = 1, MINIMUM__VERSION = 1;
	public static final TypeToken<WorldConfig> TOKEN = TypeToken.of(WorldConfig.class);
	public static final Immutable VANILLA = new WorldConfig().toImmutable();

	@Setting(value = "BlockReactions")
	public Map<BlockState, MovementReaction> blockReactions = new HashMap<>();
	@Setting(value = "StickyBlocks")
	public Map<BlockState, Boolean> stickyBlocks = new HashMap<>();
	@Setting(value = "MaxBlocks")
	public int maxBlocks = 12;

	public Immutable toImmutable() {
		return new Immutable(this.blockReactions, this.stickyBlocks, this.maxBlocks);
	}

	public static WorldConfig load(Path file) throws IOException, ObjectMappingException {
		ConfigurationLoader<CommentedConfigurationNode> loader = IOUtil.createConfigLoader(file);

		CommentedConfigurationNode root = loader.load();
		int version = root.getNode("Version").getInt();
		if ((version > CURRENT_VERSION || version < MINIMUM__VERSION) && IOUtil.backup(file).isPresent()) {
			SuperPiston.LOGGER.info("Your config version is not supported. A new one will be generated.");
			root = loader.createEmptyNode();
		}

		ConfigurationNode cfgNode = root.getNode("Config");
		WorldConfig cfg = cfgNode.getValue(TOKEN, new WorldConfig());

		cfg.maxBlocks = clamp(cfg.maxBlocks, 1, 500);

		root.getNode("Version").setValue(CURRENT_VERSION);
		cfgNode.setValue(TOKEN, cfg);
		loader.save(root);
		return cfg;
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
