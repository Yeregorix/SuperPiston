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
import net.smoofyuniverse.superpiston.event.PistonEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id = "superpiston", name = "SuperPiston", version = "1.0.0", authors = "Yeregorix", description = "Allows to modify vanilla pistons")
public class SuperPiston {
	public static final Logger LOGGER = LoggerFactory.getLogger("SuperPiston");
	private static SuperPiston instance;

	@Inject
	private PluginContainer container;
	@Inject
	private Game game;

	public SuperPiston() {
		if (instance != null)
			throw new IllegalStateException();
		instance = this;
	}

	@Listener
	public void onGamePreInit(GamePreInitializationEvent e) {
		this.game.getEventManager().registerListeners(this, new PistonEventListener());
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
