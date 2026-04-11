package dev.latvian.mods.vidlib.feature.progressqueue;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiHooks;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;

import java.util.List;

public class ProgressQueueImGui {
	public static void handle(ImGraphics graphics) {
		synchronized (ProgressQueue.ACTIVE) {
			handle0(graphics, ProgressQueue.ACTIVE);
		}
	}

	private static void handle0(ImGraphics graphics, List<ProgressQueue> queueList) {
		if (queueList.isEmpty()) {
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

		var queueItr = queueList.iterator();
		int queueCount = 0;

		while (queueItr.hasNext()) {
			var queue = queueItr.next();

			if (queue.hideInGame && graphics.inGame) {
				continue;
			}

			int maxItemCount = queue.items.size();
			int done = 0;

			for (var fileProgress : queue.items) {
				if (fileProgress.isDone()) {
					done++;
				}
			}

			if (done >= maxItemCount && queue.errors.isEmpty()) {
				queue.active = false;
				queueItr.remove();
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

			ImGuiUtils.BOOLEAN.set(queue.open);

			if (queue.canCancel || !queue.errors.isEmpty() ? ImGui.begin(windowId, ImGuiUtils.BOOLEAN, flags) : ImGui.begin(windowId, flags)) {
				queue.open = ImGuiUtils.BOOLEAN.get();

				ImGui.pushItemWidth(-1F);

				if (maxItemCount > 1) {
					float p = (float) done / (float) maxItemCount;
					ImGui.progressBar(p, -1F, 20F, done + "/" + maxItemCount);
				}

				for (var item : queue.items) {
					if (item.isVisible()) {
						long progress = item.progress.get();
						long size = item.size.get();

						if (size > 0L) {
							var l = item.label;

							if (!l.isEmpty()) {
								ImGui.text(l);
							}

							float p = Math.clamp((float) ((double) progress / (double) size), 0F, 1F);
							ImGui.progressBar(p, -1F, 20F, item.nameFunction.getName(progress, size));
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

				if (!queue.open) {
					queue.errors.clear();
				}
			}

			ImGui.end();
			queueCount++;
		}

		graphics.popStack();
	}
}
