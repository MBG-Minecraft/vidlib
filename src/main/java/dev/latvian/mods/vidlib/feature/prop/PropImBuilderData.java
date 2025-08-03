package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderSupplier;

public record PropImBuilderData<T>(PropData<?, T> data, ImBuilderSupplier<T> supplier) {
}
