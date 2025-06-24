package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.util.Cast;
import imgui.ImGui;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

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

	private final CachedBuilder<T>[] options;
	private final CachedBuilder<T>[] selectedBuilder;
	public boolean deleted = false;

	public ImBuilderWrapper(ImBuilderHolderList<T> options) {
		this.options = new CachedBuilder[options.list().size()];
		this.selectedBuilder = new CachedBuilder[1];

		for (int i = 0; i < this.options.length; i++) {
			this.options[i] = new CachedBuilder<>(options.list().get(i));

			if (this.options[i].holder.isDefault()) {
				this.selectedBuilder[0] = this.options[i];
			}
		}
	}

	public boolean selectUnit(T value) {
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

		var builder = selectedBuilder[0].get();

		if (builder != null) {
			builder.set(Cast.to(value));
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		deleted = false;
		// selectedBuilder[0] = null;
		var update = graphics.combo("###select-builder", "", selectedBuilder, options);
		var builder = selectedBuilder[0].get();

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
		var builder = selectedBuilder[0].get();
		return builder != null && builder.isValid();
	}

	@Override
	@Nullable
	public T build() {
		var builder = selectedBuilder[0].get();
		return builder == null ? null : builder.build();
	}
}
