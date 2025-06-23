package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.math.KMath;
import imgui.ImGui;

public class MemoryUsagePanel extends AdminPanel {
	public static final MemoryUsagePanel INSTANCE = new MemoryUsagePanel();

	private static long toMiB(long bytes) {
		return bytes / 1024L / 1024L;
	}

	private MemoryUsagePanel() {
		super("memory-usage", "Memory Usage");
	}

	@Override
	public void content(ImGraphics graphics) {
		long maxMemory = Runtime.getRuntime().maxMemory();
		long totalMemory = Runtime.getRuntime().totalMemory();
		long freeMemory = Runtime.getRuntime().freeMemory();
		long usedMemory = totalMemory - freeMemory;

		ImGui.pushItemWidth(-1F);

		ImGui.text("Memory: %2d%% %03d/%03dMB".formatted(usedMemory * 100L / maxMemory, toMiB(usedMemory), toMiB(maxMemory)));
		ImGui.progressBar(KMath.clamp(usedMemory / (float) maxMemory, 0F, 1F), 0F, 20F, "");

		ImGui.text("Allocated: %2d%% %03dMB".formatted(totalMemory * 100L / maxMemory, toMiB(totalMemory)));
		ImGui.progressBar(KMath.clamp(totalMemory / (float) maxMemory, 0F, 1F), 0F, 20F, "");

		ImGui.popItemWidth();
	}
}
