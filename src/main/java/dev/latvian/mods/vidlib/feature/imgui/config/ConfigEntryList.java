package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record ConfigEntryList(Component label, List<ConfigEntry<?>> config, boolean[] open) {
	public static ConfigEntryList build(Component label, Consumer<List<ConfigEntry<?>>> list) {
		var list1 = new ArrayList<ConfigEntry<?>>();
		list.accept(list1);
		return new ConfigEntryList(label, List.copyOf(list1), new boolean[]{true});
	}

	public ConfigEntryList(Component label, List<ConfigEntry<?>> config) {
		this(label, config, new boolean[]{true});
	}

	public ConfigEntryList close() {
		open[0] = false;
		return this;
	}

	public boolean isDefault() {
		for (var config : config) {
			if (!config.isDefault()) {
				return false;
			}
		}

		return true;
	}

	public void reset(ImGraphics graphics) {
		for (var config : config) {
			if (!config.isDefault()) {
				config.set(Cast.to(config.key.defaultValue()));
				config.update(graphics, true);
			}
		}
	}
}
