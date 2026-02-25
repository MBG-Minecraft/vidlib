package dev.latvian.mods.vidlib.feature.progressqueue;

import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiHooks;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;

public class ProgressQueueImGui {
	public static void handle(ImGraphics graphics) {
		if (ProgressQueue.ACTIVE.isEmpty()) {
			return;
		}

		var queueItr = ProgressQueue.ACTIVE.iterator();
		int windowCreated = 0;
		int queueCount = 0;

		while (queueItr.hasNext()) {
			var queue = queueItr.next();

			if (queue.hideInGame && graphics.inGame) {
				continue;
			}

			int maxFileCount = queue.items.size();
			int done = 0;

			for (var fileProgress : queue.items) {
				if (fileProgress.isDone()) {
					done++;
				}
			}

			if (done >= maxFileCount && queue.errors.isEmpty()) {
				queueItr.remove();
				continue;
			}

			if (windowCreated == 0) {
				windowCreated = 1;
				ImVec2 windowPos, windowSize;

				if (ImGuiHooks.centralDockNode != null) {
					windowPos = ImGuiHooks.centralDockNode.getPos();
					windowSize = ImGuiHooks.centralDockNode.getSize();
				} else {
					var window = ImGui.getMainViewport();
					windowPos = window.getPos();
					windowSize = window.getSize();
				}

				var barHeight = ImGuiHooks.scaleBarHeight(ImGuiUtils.getDpiScale());

				ImGui.setNextWindowContentSize(300F, 0F);
				ImGui.setNextWindowPos(windowPos.x + windowSize.x - 300F - barHeight, windowPos.y + barHeight / 1.5F);

				int flags = ImGuiWindowFlags.NoDocking
					| ImGuiWindowFlags.NoNav
					| ImGuiWindowFlags.NoDecoration
					| ImGuiWindowFlags.AlwaysAutoResize
					| ImGuiWindowFlags.NoSavedSettings;

				if (ImGui.begin("###progress-queue", flags)) {
					windowCreated = 2;
					ImGui.pushItemWidth(-1F);
				}
			}

			if (windowCreated != 2) {
				continue;
			}

			if (queueCount > 0) {
				ImGui.separator();
			}

			ImGui.pushID(queueCount);

			if (!queue.topText.isEmpty()) {
				ImGui.textWrapped(queue.topText);
			}

			if (maxFileCount > 1) {
				ImGui.progressBar((float) done / (float) maxFileCount, -1F, 20F, done + "/" + maxFileCount);
			}

			for (var item : queue.items) {
				if (item.isVisible()) {
					long progress = item.progress().get();
					long size = item.size().get();

					if (size > 0L) {
						ImGui.progressBar(Math.clamp((float) ((double) progress / (double) size), 0F, 1F), -1F, 20F, item.nameFunction().getName(progress, size));
					}
				}
			}

			if (!queue.errors.isEmpty()) {
				var clear = graphics.button("Clear Errors###clear-errors", ImColorVariant.RED);

				graphics.pushStack();
				graphics.setErrorText();

				for (var error : queue.errors) {
					if (queue.errors.size() > 1) {
						ImGui.bullet();
					}

					ImGui.textWrapped(error);
				}

				graphics.popStack();

				if (clear) {
					queue.errors.clear();
				}
			}

			if (!queue.bottomText.isEmpty()) {
				ImGui.textWrapped(queue.bottomText);
			}

			ImGui.popID();
			queueCount++;
		}

		if (windowCreated > 0) {
			ImGui.popItemWidth();
			ImGui.end();
		}
	}
}
