package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;

public class StopwatchPanel extends AdminPanel {
	public static final StopwatchPanel INSTANCE = new StopwatchPanel();

	public long stopwatch = 0L;
	public long stopwatchStart = 0L;

	public StopwatchPanel() {
		super("stopwatch", "Stopwatch");
	}

	@Override
	public void content() {
		long now = System.currentTimeMillis();
		long sw = stopwatch;

		if (stopwatchStart != 0L) {
			sw += now - stopwatchStart;
		}

		// ImGui.pushFont(ImFonts.getCousine25());
		ImGui.text("%02d:%02d:%03d".formatted(sw / 60000L, (sw / 1000L) % 60, sw % 1000L));
		// ImGui.popFont();

		ImGui.button(stopwatchStart == 0 ? "Start" : "Stop");

		if (ImGui.isItemClicked()) {
			if (stopwatchStart == 0L) {
				stopwatchStart = now;
			} else {
				stopwatch += now - stopwatchStart;
				stopwatchStart = 0L;
			}
		}

		ImGui.button("Reset");

		if (ImGui.isItemClicked()) {
			stopwatch = 0L;
			stopwatchStart = stopwatchStart == 0L ? 0L : now;
		}
	}
}
