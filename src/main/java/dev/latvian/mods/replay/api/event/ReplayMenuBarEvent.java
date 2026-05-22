package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplaySession;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;

public class ReplayMenuBarEvent extends ReplayImGuiEventEvent {
	public ReplayMenuBarEvent(ReplayAPI api, ReplaySession session, ImGraphics graphics) {
		super(api, session, graphics);
	}
}
