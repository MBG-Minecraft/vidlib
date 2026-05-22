package dev.latvian.mods.vidlib.feature.imgui.builder;

import java.util.LinkedHashSet;
import java.util.Set;

public class SetImBuilder<T> extends CollectionImBuilder<T, Set<T>> {
	public SetImBuilder(ImBuilderType<T> elementType) {
		super(elementType);
	}

	@Override
	public Set<T> createCollection(int size) {
		return new LinkedHashSet<>(size);
	}
}
