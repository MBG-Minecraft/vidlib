package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ImBuilderWrapper<T> implements ImBuilder<T> {
	private static class CachedBuilder<T> implements StringRepresentable {
		private final ImBuilderHolder<T> holder;
		private ImBuilder<? extends T> builder;

		private CachedBuilder(ImBuilderHolder<T> holder) {
			this.holder = holder;
		}

		public ImBuilder<? extends T> get() {
			if (builder == null) {
				builder = holder.get();
			}

			return builder;
		}

		@Override
		public String getSerializedName() {
			return holder.name();
		}
	}

	private final ImBuilderHolderList<T> originalOptions;
	private List<CachedBuilder<T>> options;
	private final CachedBuilder<T>[] selectedBuilder;
	public boolean deleted = false;

	public ImBuilderWrapper(ImBuilderHolderList<T> options) {
		this.originalOptions = options;
		this.selectedBuilder = new CachedBuilder[1];
	}

	private List<CachedBuilder<T>> getOptions() {
		if (options == null) {
			options = new ArrayList<>(originalOptions.list().size());
			selectedBuilder[0] = null;

			for (var originalOption : originalOptions.list()) {
				var option = new CachedBuilder<>(originalOption);
				options.add(option);

				if (option.holder.isDefault()) {
					selectedBuilder[0] = option;
				}
			}
		}

		return options;
	}

	public boolean selectUnit(T value) {
		options = getOptions();

		for (var option : options) {
			if (option.get() instanceof ImBuilder.Unit<?> unit && unit.value() == value) {
				selectedBuilder[0] = option;
				return true;
			}
		}

		return false;
	}

	@Override
	public void set(T value) {
		if (selectUnit(value)) {
			return;
		}

		var builder = getBuilder();

		if (builder != null) {
			builder.set(Cast.to(value));
		}
	}

	public ImBuilder<? extends T> getBuilder() {
		return selectedBuilder[0] == null ? null : selectedBuilder[0].get();
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		deleted = false;
		options = getOptions();

		var update = graphics.combo("###select-builder", "Select...", selectedBuilder, options);
		var builder = getBuilder();

		if (builder == null) {
			return ImUpdate.NONE;
		}

		ImGui.indent();
		update = update.or(builder.imgui(graphics));
		ImGui.unindent();
		return update;
	}

	@Override
	public boolean isValid() {
		var builder = getBuilder();
		return builder != null && builder.isValid();
	}

	@Override
	@Nullable
	public T build() {
		var builder = getBuilder();
		return builder == null ? null : builder.build();
	}
}
