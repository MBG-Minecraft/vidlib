package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplaySession;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import imgui.ImGui;

public class ReplayRenderFilterMenuEvent extends ReplayImGuiEventEvent {
	public ReplayRenderFilterMenuEvent(ReplayAPI api, ReplaySession session, ImGraphics graphics) {
		super(api, session, graphics);
	}

	@Override
	public boolean beginSection(String id, String label) {
		return ImGui.beginTabItem(label + "###" + id);
	}

	@Override
	public void endSection() {
		ImGui.endTabItem();
	}
}
