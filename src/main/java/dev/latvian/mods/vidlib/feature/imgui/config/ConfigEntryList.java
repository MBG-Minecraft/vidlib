package dev.latvian.mods.vidlib.feature.imgui.config;

import imgui.type.ImBoolean;
import net.minecraft.network.chat.Component;

import java.util.List;

public record ConfigEntryList(Component label, List<ConfigEntry<?>> config, ImBoolean open) {
	public ConfigEntryList(Component label, List<ConfigEntry<?>> config) {
		this(label, config, new ImBoolean(true));
	}
}
