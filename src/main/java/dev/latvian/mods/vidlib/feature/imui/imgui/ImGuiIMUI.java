package dev.latvian.mods.vidlib.feature.imui.imgui;

import dev.latvian.mods.vidlib.feature.imui.IMUI;
import imgui.ImGui;

public class ImGuiIMUI implements IMUI {
	@Override
	public void pushID(int id) {
		ImGui.pushID(id);
	}

	@Override
	public void pushID(String id) {
		ImGui.pushID(id);
	}

	@Override
	public void popID() {
		ImGui.popID();
	}

	@Override
	public void text(String text) {
		ImGui.text(text);
	}
}
