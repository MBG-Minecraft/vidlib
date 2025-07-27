package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;

public record PropImBuilderData<T>(PropData<?, T> data, ImBuilder<? extends T> builder) {
}
