package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplaySession;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import imgui.ImGui;

public abstract class ReplayImGuiEventEvent extends ReplaySessionEvent {
	private final ImGraphics graphics;

	public ReplayImGuiEventEvent(ReplayAPI api, ReplaySession session, ImGraphics graphics) {
		super(api, session);
		this.graphics = graphics;
	}

	public ImGraphics getGraphics() {
		return graphics;
	}

	public boolean beginSection(String id, String label) {
		if (!label.isEmpty()) {
			ImGuiUtils.separatorWithText(label);
		}

		ImGui.pushID("###" + id);
		return true;
	}

	public void endSection() {
		ImGui.popID();
	}
}
