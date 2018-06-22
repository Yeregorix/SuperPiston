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

package net.smoofyuniverse.superpiston;

import com.google.inject.Inject;
import net.smoofyuniverse.superpiston.bstats.MetricsLite;
import net.smoofyuniverse.superpiston.config.PistonConfig;
import net.smoofyuniverse.superpiston.config.PistonConfig.Immutable;
import net.smoofyuniverse.superpiston.event.WorldEventListener;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Plugin(id = "superpiston", name = "SuperPiston", version = "1.0.0", authors = "Yeregorix", description = "Allows to modify vanilla pistons")
public class SuperPiston {
	public static final int CURRENT_CONFIG_VERSION = 1, MINIMUM_CONFIG_VERSION = 1;
	public static final Logger LOGGER = LoggerFactory.getLogger("SuperPiston");
	private static SuperPiston instance;
	private final Map<String, Immutable> configs = new HashMap<>();
	@Inject
	private Game game;
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	@Inject
	private PluginContainer container;
	@Inject
	private GuiceObjectMapperFactory factory;
	@Inject
	private MetricsLite metrics;
	private ConfigurationOptions configOptions;
	private Path worldConfigsDir;

	public SuperPiston() {
		if (instance != null)
			throw new IllegalStateException();
		instance = this;
	}

	@Listener
	public void onGamePreInit(GamePreInitializationEvent e) {
		this.worldConfigsDir = this.configDir.resolve("worlds");
		try {
			Files.createDirectories(this.worldConfigsDir);
		} catch (IOException ignored) {
		}
		this.configOptions = ConfigurationOptions.defaults().setObjectMapperFactory(this.factory).setShouldCopyDefaults(true);

		this.game.getEventManager().registerListeners(this, new WorldEventListener());
	}

	@Listener
	public void onGameReload(GameReloadEvent e) {
		this.configs.clear();

		for (World w : this.game.getServer().getWorlds())
			loadConfig(w.getName());
	}

	public void loadConfig(String worldName) {
		worldName = worldName.toLowerCase();

		LOGGER.info("Loading configuration for world " + worldName + " ..");
		try {
			Path file = this.worldConfigsDir.resolve(worldName + ".conf");
			ConfigurationLoader<CommentedConfigurationNode> loader = createConfigLoader(file);

			CommentedConfigurationNode root = loader.load();
			int version = root.getNode("Version").getInt();
			if ((version > CURRENT_CONFIG_VERSION || version < MINIMUM_CONFIG_VERSION) && backupFile(file)) {
				LOGGER.info("Your config version is not supported. A new one will be generated.");
				root = loader.createEmptyNode();
			}

			ConfigurationNode cfgNode = root.getNode("Config");
			PistonConfig cfg = cfgNode.getValue(PistonConfig.TOKEN, new PistonConfig());

			if (cfg.blockReactions == null)
				cfg.blockReactions = new HashMap<>();

			if (cfg.stickyBlocks == null)
				cfg.stickyBlocks = new HashMap<>();

			if (cfg.maxBlocks < 1)
				cfg.maxBlocks = 1;

			version = CURRENT_CONFIG_VERSION;
			root.getNode("Version").setValue(version);
			cfgNode.setValue(PistonConfig.TOKEN, cfg);
			loader.save(root);

			this.configs.put(worldName, cfg.toImmutable());
		} catch (Exception e) {
			LOGGER.error("Failed to load configuration for world " + worldName, e);
		}
	}

	public ConfigurationLoader<CommentedConfigurationNode> createConfigLoader(Path file) {
		return HoconConfigurationLoader.builder().setPath(file).setDefaultOptions(this.configOptions).build();
	}

	public Optional<PistonConfig.Immutable> getConfig(String worldName) {
		return Optional.ofNullable(this.configs.get(worldName.toLowerCase()));
	}

	public PluginContainer getContainer() {
		return this.container;
	}

	public static boolean backupFile(Path file) throws IOException {
		if (!Files.exists(file))
			return false;

		String fn = file.getFileName() + ".backup";
		Path backup = null;
		for (int i = 0; i < 100; i++) {
			backup = file.resolveSibling(fn + i);
			if (!Files.exists(backup))
				break;
		}
		Files.move(file, backup);
		return true;
	}

	public static SuperPiston get() {
		if (instance == null)
			throw new IllegalStateException("Instance not available");
		return instance;
	}
}
