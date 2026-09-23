package dev.mrbeastgaming.mods.hub.client;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.mrbeastgaming.mods.hub.api.data.HubUser;
import dev.mrbeastgaming.mods.hub.api.gateway.HubWorldsResponse;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import java.util.List;

public class HubDrivePanel extends Panel {
	public static final HubDrivePanel INSTANCE = new HubDrivePanel();
	public static final ImString SEARCH = ImGuiUtils.resizableString();
	public static final HubProject[] PROJECT_FILTER = new HubProject[1];
	public static final HubUser[] USER_FILTER = new HubUser[1];

	public boolean reload = true;

	public HubDrivePanel() {
		super("hub-drive", "Drive");
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
	public int setup(ImGraphics graphics) {
		var viewport = ImGui.getMainViewport();
		float yOff = viewport.getWorkPosY() - viewport.getPosY();
		float popupWidth = viewport.getWorkSizeX();
		float popupHeight = viewport.getWorkSizeY() - yOff;

		if (popupWidth <= 0F || popupHeight <= 0F) {
			return -1;
		}

		ImGui.setNextWindowPos(viewport.getWorkPos().x, viewport.getWorkPosY());
		ImGui.setNextWindowSize(popupWidth, popupHeight);
		return super.setup(graphics) | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoDocking;
	}

	@Override
	public void content(ImGraphics graphics) {
		if (reload) {
			/*
			HubWorldsData.update(data -> {
				var savesDir = PlatformHelper.CURRENT.getGameDirectory().resolve("saves");
				var templatesDir = PlatformHelper.CURRENT.getGameDirectory().resolve("saves-templates");

				try {
					if (Files.notExists(savesDir)) {
						Files.createDirectories(savesDir);
					}

					if (Files.notExists(templatesDir)) {
						Files.createDirectories(templatesDir);
					}
				} catch (Exception ex) {
				}

				PROJECT_FILTER[0] = PROJECT_FILTER[0] == null ? null : data.relevantProjects().get(PROJECT_FILTER[0].id().raw());
				USER_FILTER[0] = USER_FILTER[0] == null ? null : data.relevantUsers().get(USER_FILTER[0].id().raw());
			});
			 */

			reload = false;
		}

		ImGui.text("WIP");
	}
}
