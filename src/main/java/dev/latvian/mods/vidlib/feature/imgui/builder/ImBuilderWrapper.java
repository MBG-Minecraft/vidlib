package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ImBuilderWrapper<T> implements ImBuilder<T> {
	public static class Factory<T> {
		private final Supplier<? extends ImBuilderEvent<T>> event;
		private List<ImBuilderHolder<T>> options;

		public Factory(Supplier<? extends ImBuilderEvent<T>> event) {
			this.event = event;
			this.options = null;
		}

		public synchronized List<ImBuilderHolder<T>> getOptions() {
			if (options == null) {
				var e = event.get();
				NeoForge.EVENT_BUS.post(e);
				options = List.copyOf(e.getBuilderHolders());
			}

			return options;
		}

		public ImBuilder<T> create(@Nullable T defaultValue) {
			var builder = new ImBuilderWrapper<>(this);

			if (defaultValue != null) {
				builder.set(defaultValue);
			}

			return builder;
		}
	}

	private static class CachedBuilder<T> {
		private final ImBuilderHolder<T> holder;
		private ImBuilder<? extends T> builder;

		private CachedBuilder(ImBuilderHolder<T> holder) {
			this.holder = holder;
		}

		public ImBuilder<? extends T> get() {
			if (builder == null) {
				builder = holder.type().get();
			}

			return builder;
		}
	}

	private final Factory<T> factory;
	private List<CachedBuilder<T>> options;
	private ImBuilder<? extends T> selectedBuilder;

	public ImBuilderWrapper(Factory<T> factory) {
		this.factory = factory;
		this.selectedBuilder = null;
	}

	private List<CachedBuilder<T>> getOptions() {
		if (options == null) {
			var originalOptions = factory.getOptions();

			options = new ArrayList<>(originalOptions.size());

			for (var originalOption : originalOptions) {
				var option = new CachedBuilder<>(originalOption);
				options.add(option);

				if (selectedBuilder == null && option.holder.isDefault()) {
					selectedBuilder = option.get();
				}
			}
		}

		return options;
	}

	@Override
	public void set(T value) {
		selectedBuilder = null;

		if (value == null) {
			return;
		}

		if (value instanceof ImBuilderWithHolder.Factory bf) {
			selectedBuilder = Cast.to(bf.createImBuilder());
			selectedBuilder.set(Cast.to(value));
			return;
		}

		var options = getOptions();

		for (var option : options) {
			if (option.get() instanceof ImBuilder.Unit<?> unit && unit.value() == value) {
				selectedBuilder = (ImBuilder<? extends T>) unit;
				return;
			}
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		options = getOptions();

		var update = ImUpdate.NONE;

		if (ImGui.beginCombo("###select-builder", selectedBuilder == null ? "Select..." : selectedBuilder.getDisplayName(), 0)) {
			for (int i = 0; i < options.size(); i++) {
				var option = options.get(i);
				boolean isSelected = selectedBuilder != null && option.holder.isSame(selectedBuilder);

				if (ImGui.selectable(options.get(i).holder.name() + "###" + i, isSelected)) {
					selectedBuilder = option.get();
					update = ImUpdate.FULL;
				}

				if (isSelected) {
					ImGui.setItemDefaultFocus();
				}
			}

			ImGui.endCombo();
		}

		if (selectedBuilder == null) {
			return ImUpdate.NONE;
		}

		return update.or(selectedBuilder.imgui(graphics));
	}

	@Override
	public boolean isValid() {
		return selectedBuilder != null && selectedBuilder.isValid();
	}

	@Override
	@Nullable
	public T build() {
		return selectedBuilder == null ? null : selectedBuilder.build();
	}
}
