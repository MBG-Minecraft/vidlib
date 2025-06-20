package dev.latvian.mods.vidlib.feature.imgui.config;

import net.minecraft.network.chat.Component;

import java.util.List;

public record ConfigEntryList(Component label, List<ConfigEntry<?>> config) {
}
