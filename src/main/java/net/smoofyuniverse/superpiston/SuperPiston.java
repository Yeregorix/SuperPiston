/*
 * Copyright (c) 2018-2020 Hugo Dupanloup (Yeregorix)
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
import net.smoofyuniverse.ore.update.UpdateChecker;
import net.smoofyuniverse.superpiston.config.world.WorldConfig;
import net.smoofyuniverse.superpiston.event.WorldEventListener;
import net.smoofyuniverse.superpiston.impl.internal.InternalServer;
import net.smoofyuniverse.superpiston.util.IOUtil;
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
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.smoofyuniverse.superpiston.util.MathUtil.clamp;

@Plugin(id = "superpiston", name = "SuperPiston", version = "1.0.7", authors = "Yeregorix", description = "Allows to modify vanilla pistons")
public class SuperPiston {
	public static final Logger LOGGER = LoggerFactory.getLogger("SuperPiston");
	private static SuperPiston instance;

	@Inject
	private Game game;
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	@Inject
	private PluginContainer container;
	@Inject
	private GuiceObjectMapperFactory factory;

	private ConfigurationOptions configOptions;
	private Path worldConfigsDir;

	private final Map<String, WorldConfig.Immutable> configs = new HashMap<>();

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
		this.configOptions = ConfigurationOptions.defaults().withObjectMapperFactory(this.factory);

		this.game.getEventManager().registerListeners(this, new WorldEventListener());

		this.game.getEventManager().registerListeners(this, new UpdateChecker(LOGGER, this.container,
				createConfigLoader(this.configDir.resolve("update.conf")), "Yeregorix", "SuperPiston"));
	}

	@Listener
	public void onGameReload(GameReloadEvent e) {
		this.configs.clear();
		this.game.getServer().getWorlds().forEach(this::loadConfig);
	}

	public void loadConfig(World world) {
		String name = world.getName();

		LOGGER.info("Loading configuration for world " + name + " ..");
		try {
			Path file = this.worldConfigsDir.resolve(name + ".conf");
			ConfigurationLoader<CommentedConfigurationNode> loader = createConfigLoader(file);

			CommentedConfigurationNode root = loader.load();
			int version = root.getNode("Version").getInt();
			if ((version > WorldConfig.CURRENT_VERSION || version < WorldConfig.MINIMUM__VERSION) && IOUtil.backupFile(file)) {
				LOGGER.info("Your config version is not supported. A new one will be generated.");
				root = loader.createEmptyNode();
			}

			ConfigurationNode cfgNode = root.getNode("Config");
			WorldConfig cfg = cfgNode.getValue(WorldConfig.TOKEN, new WorldConfig());

			if (cfg.blockReactions == null)
				cfg.blockReactions = new HashMap<>();

			if (cfg.stickyBlocks == null)
				cfg.stickyBlocks = new HashMap<>();

			cfg.maxBlocks = clamp(cfg.maxBlocks, 1, 500);

			version = WorldConfig.CURRENT_VERSION;
			root.getNode("Version").setValue(version);
			cfgNode.setValue(WorldConfig.TOKEN, cfg);
			loader.save(root);

			this.configs.put(name, cfg.toImmutable());
		} catch (Exception e) {
			LOGGER.error("Failed to load configuration for world " + name, e);
		}
	}

	@Listener
	public void onServerStarted(GameStartedServerEvent e) {
		if (this.game.getServer() instanceof InternalServer)
			LOGGER.info("SuperPiston " + this.container.getVersion().orElse("?") + " was loaded successfully.");
		else
			LOGGER.error("!!WARNING!! SuperPiston was not loaded correctly. Be sure that the jar file is at the root of your mods folder!");
	}

	public ConfigurationLoader<CommentedConfigurationNode> createConfigLoader(Path file) {
		return HoconConfigurationLoader.builder().setPath(file).setDefaultOptions(this.configOptions).build();
	}

	public Optional<WorldConfig.Immutable> getConfig(World world) {
		return Optional.ofNullable(this.configs.get(world.getName()));
	}

	public PluginContainer getContainer() {
		return this.container;
	}

	public static SuperPiston get() {
		if (instance == null)
			throw new IllegalStateException("Instance not available");
		return instance;
	}
}
