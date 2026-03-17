package dev.latvian.mods.vidlib.feature.progressqueue;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiHooks;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;

public class ProgressQueueImGui {
	public static void handle(ImGraphics graphics) {
		if (ProgressQueue.ACTIVE_COUNT.get() <= 0) {
			return;
		}

		ImVec2 windowPos, windowSize;

		if (ImGuiHooks.centralDockNode != null) {
			windowPos = ImGuiHooks.centralDockNode.getPos();
			windowSize = ImGuiHooks.centralDockNode.getSize();
		} else {
			var window = ImGui.getMainViewport();
			windowPos = window.getPos();
			windowSize = window.getSize();
		}

		var offset = ImGuiHooks.mainMenuBarHeight / 2F;

		float windowX = windowPos.x + windowSize.x - 312F - offset;
		float windowY = windowPos.y + offset;

		graphics.pushStack();
		graphics.setWindowRounding(8F);
		graphics.setWindowPadding(6F, 6F);

		var queueItr = ProgressQueue.ACTIVE.iterator();
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
				queue.active = false;
				queueItr.remove();
				ProgressQueue.ACTIVE_COUNT.decrementAndGet();
				continue;
			}

			ImGui.setNextWindowSizeConstraints(300F, 20F, 300F, 600F);
			ImGui.setNextWindowPos(windowX, windowY);

			int flags = ImGuiWindowFlags.NoDocking
				| ImGuiWindowFlags.NoNav
				| ImGuiWindowFlags.NoResize
				| ImGuiWindowFlags.NoCollapse
				| ImGuiWindowFlags.AlwaysAutoResize
				| ImGuiWindowFlags.NoSavedSettings;

			var windowId = queue.topText + "###progress-queue-" + queueCount;

			if (queue.canCancel || !queue.errors.isEmpty() ? ImGui.begin(windowId, queue.open, flags) : ImGui.begin(windowId, flags)) {
				ImGui.pushItemWidth(-1F);

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
					graphics.pushStack();
					graphics.setErrorText();

					for (var error : queue.errors) {
						error.push(graphics);

						if (queue.errors.size() > 1) {
							ImGui.bullet();
						}

						ImGui.textWrapped(error.text());
						error.pop(graphics);
					}

					graphics.popStack();
				}

				if (!queue.bottomText.isEmpty()) {
					ImGui.separator();
					ImGui.textWrapped(queue.bottomText);
				}

				ImGui.popItemWidth();
				windowY += ImGui.getWindowSizeY() + offset;

				if (!queue.open.get()) {
					queue.errors.clear();
				}
			}

			ImGui.end();
			queueCount++;
		}

		graphics.popStack();
	}
}
