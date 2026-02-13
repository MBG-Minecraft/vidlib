package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.util.StringUtils;
import imgui.ImGui;

public class GlobalStopwatchPanel extends Panel {
	public static final GlobalStopwatchPanel INSTANCE = new GlobalStopwatchPanel();

	public GlobalStopwatchPanel() {
		super("stopwatch", "Stopwatch");
		this.style = PanelStyle.MINIMAL;
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.inGame) {
			close();
			return;
		}

		long stopwatch = graphics.mc.getOptional(InternalServerData.GLOBAL_STOPWATCH);
		long stopwatchStart = graphics.mc.getOptional(InternalServerData.GLOBAL_STOPWATCH_START);
		boolean update = false;

		long now = graphics.mc.vl$getGameTime();
		long sw = stopwatch;

		if (stopwatchStart != 0L) {
			sw += now - stopwatchStart;
		}

		if (!graphics.isAdmin) {
			ImGui.beginDisabled();
		}

		if (ImGui.smallButton(ImIcons.RELOAD.toString())) {
			stopwatch = 0L;
			stopwatchStart = stopwatchStart == 0L ? 0L : now;
			update = true;
		}

		ImGui.sameLine();

		if (ImGui.smallButton(stopwatchStart == 0L ? ImIcons.PLAY.toString() : ImIcons.PAUSE.toString())) {
			if (stopwatchStart == 0L) {
				stopwatchStart = now;
			} else {
				stopwatch += now - stopwatchStart;
				stopwatchStart = 0L;
			}

			update = true;
		}

		if (!graphics.isAdmin) {
			ImGui.endDisabled();
		}

		ImGui.sameLine();
		ImGui.text(StringUtils.timer(sw * 50L));

		if (update && graphics.isAdmin) {
			graphics.mc.set(InternalServerData.GLOBAL_STOPWATCH, stopwatch);
			graphics.mc.updateServerDataValue(InternalServerData.GLOBAL_STOPWATCH, stopwatch);
			graphics.mc.set(InternalServerData.GLOBAL_STOPWATCH_START, stopwatchStart);
			graphics.mc.updateServerDataValue(InternalServerData.GLOBAL_STOPWATCH_START, stopwatchStart);
		}
	}
}
