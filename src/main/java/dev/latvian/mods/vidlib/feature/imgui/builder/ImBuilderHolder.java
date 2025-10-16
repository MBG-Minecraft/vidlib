package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.node.Node;
import org.jetbrains.annotations.Nullable;

public record ImBuilderHolder<T>(String name, ImBuilderType<T> type, boolean isDefault) {
	public static <T> ImBuilderHolder<T> ofDefault(String name, ImBuilderType<T> factory) {
		return new ImBuilderHolder<>(name, factory, true);
	}

	public static <T> ImBuilderHolder<T> of(String name, ImBuilderType<T> factory) {
		return new ImBuilderHolder<>(name, factory, false);
	}

	public boolean isSame(ImBuilder<?> builder) {
		if (type instanceof ImBuilderType.Unit<?> u && u.unit() instanceof ImBuilder.Unit<?> u1 && u1 == builder) {
			return true;
		} else if (builder instanceof ImBuilderWithHolder<?> h) {
			return this == h.holder();
		} else {
			return false;
		}
	}

	@Nullable
	public Node asNode() {
		return type.get().asNode();
	}
}
