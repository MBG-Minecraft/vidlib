package dev.latvian.mods.vidlib.feature.imgui;

import java.util.ArrayList;
import java.util.List;

public record ImBuilderHolderList<T>(List<ImBuilderHolder<T>> list) {
	public ImBuilderHolderList() {
		this(new ArrayList<>());
	}

	public void add(ImBuilderHolder<T> type) {
		list.add(type);
	}

	public void addUnit(String name, T unit) {
		list.add(new ImBuilderHolder<>(name, () -> new ImBuilder.Unit<>(unit)));
	}
}
