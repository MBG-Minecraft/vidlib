package dev.latvian.mods.vidlib.feature.imgui.config;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderSupplier;
import org.jetbrains.annotations.Nullable;

public record ConfigEntry2<T>(DataKey<T> key, @Nullable ImBuilderSupplier<T> imgui) {
	public ConfigEntry2(DataKey<T> key) {
		this(key, (ImBuilderSupplier<T>) ImBuilderSupplier.BY_DATA_TYPE.get().get(key));
	}
}
