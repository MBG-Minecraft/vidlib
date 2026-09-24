package dev.mrbeastgaming.mods.hub.client;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.PanelStyle;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import dev.mrbeastgaming.mods.hub.api.data.HubUser;
import dev.mrbeastgaming.mods.hub.api.gateway.HubWorldsResponse;
import imgui.ImGui;
import imgui.type.ImString;

import java.util.List;

public class HubReplaysPanel extends Panel {
	public static final HubReplaysPanel INSTANCE = new HubReplaysPanel();
	public static final ImString SEARCH = ImGuiUtils.resizableString();
	public static final HubProject[] PROJECT_FILTER = new HubProject[1];
	public static final HubUser[] USER_FILTER = new HubUser[1];

	public boolean reload = true;

	public HubReplaysPanel() {
		super("hub-replays", "Replays");
		style = PanelStyle.FULLSCREEN;
		menuBar = MenuItem.root((graphics, items) -> menuBar(items));
	}

	private void menuBar(List<MenuItem> items) {
		items.add(MenuItem.item(ImIcons.RELOAD, "Reload", g -> reload = true));
		items.add(MenuItem.SEPARATOR);

		items.add(MenuItem.custom(graphics -> {
			ImGui.setNextItemWidth(300F);
			ImGui.inputTextWithHint("###search", ImIcons.SEARCH + " Search...", SEARCH);
		}));

		items.add(MenuItem.SEPARATOR);

		items.add(MenuItem.custom(graphics -> {
			ImGui.setNextItemWidth(200F);
			graphics.combo("###project-filter", PROJECT_FILTER, "Any Project", HubWorldsResponse.CURRENT.ctx().relevantProjects().values().toArray(HubProject[]::new), HubProject::name);
		}));

		items.add(MenuItem.custom(graphics -> {
			ImGui.setNextItemWidth(200F);
			graphics.combo("###user-filter", USER_FILTER, "Any User", HubWorldsResponse.CURRENT.ctx().relevantUsers().values().toArray(HubUser[]::new), HubUser::name);
		}));
	}

	@Override
	public void content(ImGraphics graphics) {
		if (reload) {
			reload = false;
		}

		ImGui.text("WIP, Use Replays -> Super Sorter 9000 for now");
	}
}
