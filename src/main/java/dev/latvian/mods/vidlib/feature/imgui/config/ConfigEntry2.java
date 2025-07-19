package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;

import java.util.function.Supplier;

public record ConfigEntry2<T>(String label, DataKey<T> key, Supplier<? extends ImBuilder<T>> imgui) {
}
