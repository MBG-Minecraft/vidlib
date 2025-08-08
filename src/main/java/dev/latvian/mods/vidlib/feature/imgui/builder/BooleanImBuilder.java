package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.type.ImBoolean;

public class BooleanImBuilder implements ImBuilder<Boolean> {
	public static final ImBuilderType<Boolean> TYPE = BooleanImBuilder::new;

	public final ImBoolean value;

	public BooleanImBuilder() {
		this.value = new ImBoolean(false);
	}

	@Override
	public void set(Boolean v) {
		value.set(v);
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return ImUpdate.full(ImGui.checkbox("###boolean", value));
	}

	@Override
	public Boolean build() {
		return value.get();
	}

	@Override
	public boolean isSmall() {
		return true;
	}
}
