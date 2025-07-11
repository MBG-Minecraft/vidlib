package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;
import imgui.type.ImBoolean;

public class EntityExplorerPanel extends AdminPanel {
	public static final EntityExplorerPanel INSTANCE = new EntityExplorerPanel();

	public final ImBoolean sortByClosest;

	public EntityExplorerPanel() {
		super("entity-explorer", "Entity Explorer");
		this.sortByClosest = new ImBoolean(false);
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.inGame) {
			close();
			return;
		}

		ImGui.text("WIP");

		ImGui.checkbox("Sort by Closest", sortByClosest);
	}
}
