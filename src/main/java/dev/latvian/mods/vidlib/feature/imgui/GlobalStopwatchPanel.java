package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.util.StringUtils;
import dev.latvian.mods.vidlib.feature.data.InternalGlobalData;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
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

		var session = ClientSessionData.of(graphics.mc);
		long stopwatch = session.dataMap.get(InternalGlobalData.GLOBAL_STOPWATCH);
		long stopwatchStart = session.dataMap.get(InternalGlobalData.GLOBAL_STOPWATCH_START);
		boolean update = false;

		long now = graphics.mc.getLevelGameTime();
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
			session.dataMap.set(InternalGlobalData.GLOBAL_STOPWATCH, stopwatch);
			graphics.mc.updateServerDataValue(InternalGlobalData.GLOBAL_STOPWATCH, stopwatch);
			session.dataMap.set(InternalGlobalData.GLOBAL_STOPWATCH_START, stopwatchStart);
			graphics.mc.updateServerDataValue(InternalGlobalData.GLOBAL_STOPWATCH_START, stopwatchStart);
		}
	}
}
