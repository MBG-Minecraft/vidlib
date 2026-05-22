package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import imgui.ImGuiStyle;

public class ReplayStyleEvent extends ReplayEvent {
	private final ImGuiStyle style;

	public ReplayStyleEvent(ReplayAPI api, ImGuiStyle style) {
		super(api);
		this.style = style;
	}

	public ImGuiStyle getStyle() {
		return style;
	}
}
