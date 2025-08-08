package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.type.ImString;

public class StringImBuilder implements ImBuilder<String> {
	public static final ImBuilderType<String> TYPE = StringImBuilder::new;

	public final ImString value;

	public StringImBuilder() {
		this.value = ImGuiUtils.resizableString();
	}

	@Override
	public void set(String v) {
		value.set(v);
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		ImGui.inputText("###string", value);
		return ImUpdate.itemEdit();
	}

	@Override
	public String build() {
		return value.get();
	}
}
