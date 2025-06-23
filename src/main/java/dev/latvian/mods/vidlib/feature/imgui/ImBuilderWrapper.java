package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;
import org.jetbrains.annotations.Nullable;

public class ImBuilderWrapper<T> implements ImBuilder<T> {
	private final ImBuilderHolder<T>[] options;
	private final ImBuilderHolder<T>[] selectedBuilder;
	public ImBuilder<? extends T> builder;

	public ImBuilderWrapper(ImBuilderHolderList<T> options) {
		this.options = options.list().toArray(new ImBuilderHolder[0]);
		this.selectedBuilder = new ImBuilderHolder[1];

		for (var option : this.options) {
			if (option.isDefault()) {
				this.selectedBuilder[0] = option;
				this.builder = option.get();
			}
		}
	}

	@Override
	public void set(T value) {
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		// selectedBuilder[0] = null;
		var update = graphics.combo("###select-builder", "", selectedBuilder, options, ImBuilderHolder::name, 0);

		if (update.isAny()) {
			builder = selectedBuilder[0].get();
		}

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
		return builder != null && builder.isValid();
	}

	@Override
	@Nullable
	public T build() {
		return builder == null ? null : builder.build();
	}
}
