package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueueImGui;
import imgui.ImGui;
import imgui.flag.ImGuiTabBarFlags;
import imgui.flag.ImGuiWindowFlags;

import java.util.ArrayList;
import java.util.List;

public class FullscreenPanel extends Panel {
	public static final FullscreenPanel INSTANCE = new FullscreenPanel();

	public final List<Panel> tabs;

	public FullscreenPanel() {
		super("fullscreen", "Fullscreen");
		this.tabs = new ArrayList<>();
	}

	@Override
	public int setup(ImGraphics graphics) {
		int flags = ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoNav | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoDocking | ImGuiWindowFlags.NoTitleBar;

		if (isUnsaved()) {
			flags |= ImGuiWindowFlags.UnsavedDocument;
		}

		if (menuBar != null) {
			flags |= ImGuiWindowFlags.MenuBar;
		}

		var viewport = ImGui.getMainViewport();
		float yOff = viewport.getWorkPosY() - viewport.getPosY();
		float popupWidth = viewport.getWorkSizeX();
		float popupHeight = viewport.getWorkSizeY() - yOff;

		if (popupWidth <= 0F || popupHeight <= 0F) {
			return -1;
		}

		ImGui.setNextWindowPos(viewport.getWorkPos().x, viewport.getWorkPosY());
		ImGui.setNextWindowSize(popupWidth, popupHeight);
		return flags;
	}

	@Override
	public void content(ImGraphics graphics) {
		if (ImGui.beginTabBar("###" + id, ImGuiTabBarFlags.Reorderable | ImGuiTabBarFlags.AutoSelectNewTabs)) {
			tabs.removeIf(panel -> panel.handle(graphics));
			ImGui.endTabBar();
		}

		if (tabs.isEmpty()) {
			close();
		} else {
			ProgressQueueImGui.handle(graphics);
		}
	}
}
