package dev.latvian.mods.vidlib.feature.imgui.builder;

import java.util.function.Supplier;

@FunctionalInterface
public interface ImBuilderType<T> extends Supplier<ImBuilder<T>> {
}
