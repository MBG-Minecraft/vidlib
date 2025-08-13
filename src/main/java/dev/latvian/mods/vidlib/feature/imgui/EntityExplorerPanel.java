package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Comparator;

public class EntityExplorerPanel extends AdminPanel {
	public static final EntityExplorerPanel INSTANCE = new EntityExplorerPanel();

	public final ImBoolean sortByClosest;
	public final ImBoolean onlyPlayers;
	public final ImString tagInput;

	public EntityExplorerPanel() {
		super("entity-explorer", "Entity Explorer");
		this.sortByClosest = new ImBoolean(false);
		this.onlyPlayers = new ImBoolean(false);
		this.tagInput = ImGuiUtils.resizableString();
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.inGame) {
			close();
			return;
		}

		var allEntities = new ArrayList<Entity>();

		if (onlyPlayers.get()) {
			allEntities.addAll(graphics.mc.level.players());
		} else {
			for (var entity : graphics.mc.level.allEntities()) {
				allEntities.add(entity);
			}
		}

		var delta = graphics.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

		ImGui.text("Entities: %,d".formatted(allEntities.size()));
		ImGui.checkbox("Sort by Closest", sortByClosest);
		ImGui.checkbox("Only Players", onlyPlayers);

		if (sortByClosest.get() && allEntities.size() >= 2) {
			var cam = graphics.mc.gameRenderer.getMainCamera().getPosition();
			allEntities.sort(Comparator.comparingDouble(p -> p.getEyePosition(delta).distanceToSqr(cam)));
		}

		ImGui.pushItemWidth(-1F);

		for (var entity : allEntities) {
			ImGui.pushID(entity.getId());

			if (graphics.collapsingHeader(entity.getName().getString() + " #" + entity.getId(), ImGuiTreeNodeFlags.NoTreePushOnOpen)) {
				// ClientProps.OPEN_PROPS.add(entity.id);
				entity.imgui(graphics, delta);
			}

			ImGui.popID();
		}

		ImGui.popItemWidth();
	}
}
