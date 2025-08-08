package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;

public record PropImBuilderData<T>(PropData<?, T> data, ImBuilderType<T> supplier) {
}
