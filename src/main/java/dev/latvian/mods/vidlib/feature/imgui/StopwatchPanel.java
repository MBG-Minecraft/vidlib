package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.util.StringUtils;
import imgui.ImGui;

import java.util.Locale;
import java.util.UUID;

public class StopwatchPanel extends Panel {
	public static void openNew() {
		new StopwatchPanel("stopwatch-" + UUID.randomUUID().toString().toLowerCase(Locale.ROOT)).open();
	}

	public long stopwatch = 0L;
	public long stopwatchStart = 0L;

	public StopwatchPanel(String id) {
		super(id, "Stopwatch");
		this.ephemeral = true;
		this.style = PanelStyle.MINIMAL;
	}

	@Override
	public void content(ImGraphics graphics) {
		long now = System.currentTimeMillis();
		long sw = stopwatch;

		if (stopwatchStart != 0L) {
			sw += now - stopwatchStart;
		}

		if (ImGui.smallButton(ImIcons.RELOAD.toString())) {
			stopwatch = 0L;
			stopwatchStart = stopwatchStart == 0L ? 0L : now;
		}

		ImGui.sameLine();

		if (ImGui.smallButton(stopwatchStart == 0 ? ImIcons.PLAY.toString() : ImIcons.PAUSE.toString())) {
			if (stopwatchStart == 0L) {
				stopwatchStart = now;
			} else {
				stopwatch += now - stopwatchStart;
				stopwatchStart = 0L;
			}
		}

		ImGui.sameLine();
		ImGui.text(StringUtils.timer(sw));
		ImGui.sameLine();

		if (ImGui.smallButton(ImIcons.CLOSE.toString())) {
			close();
		}
	}
}
