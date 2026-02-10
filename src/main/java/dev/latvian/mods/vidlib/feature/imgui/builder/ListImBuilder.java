package dev.latvian.mods.vidlib.feature.imgui.builder;

import java.util.ArrayList;
import java.util.List;

public class ListImBuilder<T> extends CollectionImBuilder<T, List<T>> {
	public ListImBuilder(ImBuilderType<T> elementType) {
		super(elementType);
	}

	@Override
	public List<T> createCollection(int size) {
		return new ArrayList<>(size);
	}
}
