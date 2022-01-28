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

package net.smoofyuniverse.superpiston.util;

import net.smoofyuniverse.superpiston.SuperPiston;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

public class RegistryUtil {

	public static <V> Map<BlockState, V> resolveBlockStates(Map<String, V> map) {
		Registry<BlockType> blockTypeRegistry = RegistryTypes.BLOCK_TYPE.get();
		BlockState.Builder blockStateBuilder = BlockState.builder();

		Map<BlockState, V> states = new HashMap<>();
		Set<String> unknownKeys = new HashSet<>(), invalidPatterns = new HashSet<>();

		for (Entry<String, V> entry : map.entrySet()) {
			String key = entry.getKey();
			V value = entry.getValue();

			boolean negate = key.startsWith("-");
			if (negate)
				key = key.substring(1);

			if (key.startsWith("regex!")) {
				key = key.substring(6);

				Pattern pattern;
				try {
					pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
				} catch (PatternSyntaxException e) {
					invalidPatterns.add(key);
					continue;
				}

				Stream<BlockState> matchedStates = blockTypeRegistry.stream()
						.flatMap(type -> type.validStates().stream())
						.filter(state -> pattern.matcher(state.toString()).matches());

				if (negate)
					matchedStates.forEach(states::remove);
				else
					matchedStates.forEach(state -> states.put(state, value));
				continue;
			}

			try {
				Optional<BlockType> type = blockTypeRegistry.findValue(ResourceKey.resolve(key));
				if (type.isPresent()) {
					List<BlockState> typeStates = type.get().validStates();
					if (negate)
						typeStates.forEach(states::remove);
					else
						typeStates.forEach(state -> states.put(state, value));
					continue;
				}
			} catch (Exception ignored) {
			}

			try {
				BlockState state = blockStateBuilder.reset().fromString(key).build();
				if (negate)
					states.remove(state);
				else
					states.put(state, value);
				continue;
			} catch (Exception ignored) {
			}

			unknownKeys.add(key);
		}

		if (!unknownKeys.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Unknown block states:");
			for (String key : unknownKeys)
				sb.append(' ').append(key);
			SuperPiston.LOGGER.warn(sb.toString());
		}

		if (!invalidPatterns.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Invalid block states patterns:");
			for (String pattern : invalidPatterns)
				sb.append(' ').append(pattern);
			SuperPiston.LOGGER.warn(sb.toString());
		}

		return states;
	}
}
