package dev.latvian.mods.vidlib.feature.note;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import imgui.ImGui;

public class NotePanel extends Panel {
	public static final NotePanel INSTANCE = new NotePanel();

	public NotePanel() {
		super("notes", "Notes");
	}

	@Override
	public void content(ImGraphics graphics) {
		ImGui.text("WIP");
	}
}
