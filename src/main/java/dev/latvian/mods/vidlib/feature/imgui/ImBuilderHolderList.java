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
}
