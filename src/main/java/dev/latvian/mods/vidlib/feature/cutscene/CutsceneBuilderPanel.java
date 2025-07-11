package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.vidlib.feature.imgui.AdminPanel;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImIcons;
import imgui.ImGui;
import imgui.flag.ImGuiTabBarFlags;

import java.util.ArrayList;
import java.util.List;

public class CutsceneBuilderPanel extends AdminPanel {
	public static final CutsceneBuilderPanel INSTANCE = new CutsceneBuilderPanel();

	public final List<CutsceneImBuilder> cutscenes;

	private CutsceneBuilderPanel() {
		super("cutscene-builder", "Cutscene Builder");
		this.cutscenes = new ArrayList<>();
		this.cutscenes.add(new CutsceneImBuilder());
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.inGame) {
			close();
			return;
		}

		if (ImGui.beginTabBar("###tabs", ImGuiTabBarFlags.AutoSelectNewTabs | ImGuiTabBarFlags.FittingPolicyScroll)) {
			for (int i = 0; i < cutscenes.size(); i++) {
				if (ImGui.beginTabItem("Cutscene #" + (i + 1) + "###" + i)) {
					cutscenes.get(i).imgui(graphics);
					ImGui.endTabItem();
				}
			}

			if (ImGui.beginTabItem(ImIcons.ADD + "###add")) {
				if (ImGui.button(ImIcons.ADD + " Cutscene###add")) {
					cutscenes.add(new CutsceneImBuilder());
				}

				ImGui.endTabItem();
			}

			ImGui.endTabBar();
		}
	}
}
