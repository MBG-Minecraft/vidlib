package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;

import java.util.Locale;
import java.util.UUID;

public class StopwatchPanel extends AdminPanel {
	public static final StopwatchPanel INSTANCE = new StopwatchPanel("stopwatch", false);

	public static void openNew() {
		new StopwatchPanel("stopwatch-" + UUID.randomUUID().toString().toLowerCase(Locale.ROOT), true).open();
	}

	public long stopwatch = 0L;
	public long stopwatchStart = 0L;

	public StopwatchPanel(String id, boolean ephemeral) {
		super(id, "Stopwatch");
		this.ephemeral = ephemeral;
		this.style = AdminPanelStyle.MINIMAL;
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

		ImGui.text("%02d:%02d:%03d".formatted(sw / 60000L, (sw / 1000L) % 60, sw % 1000L));

		if (this != INSTANCE) {
			ImGui.sameLine();

			if (ImGui.smallButton(ImIcons.CLOSE.toString())) {
				close();
			}
		}
	}
}
