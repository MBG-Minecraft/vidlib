package dev.latvian.mods.vidlib.feature.imgui.builder;

import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ImBuilderEvent<T> extends Event {
	public static class Basic<T> extends ImBuilderEvent<T> {
		private final List<ImBuilderHolder<T>> list = new ArrayList<>();

		public void add(ImBuilderHolder<T> type) {
			list.add(type);
		}

		public void addUnit(String name, T unit) {
			list.add(ImBuilderHolder.of(name, new ImBuilderType.Unit<>(new ImBuilder.Unit<>(name, unit))));
		}

		@Override
		public Collection<ImBuilderHolder<T>> getBuilderHolders() {
			return list;
		}
	}

	public abstract Collection<ImBuilderHolder<T>> getBuilderHolders();
}