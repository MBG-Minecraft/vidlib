package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public record ConfigEntryList(Component label, List<ConfigEntry<?>> config, boolean[] open) {
	public ConfigEntryList(Component label, List<ConfigEntry<?>> config) {
		this(label, config, new boolean[]{true});
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
