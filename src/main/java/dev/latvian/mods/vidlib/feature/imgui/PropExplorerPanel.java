package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PropExplorerPanel extends AdminPanel {
	public static final PropExplorerPanel INSTANCE = new PropExplorerPanel();

	public final ImBoolean sortByClosest;

	public PropExplorerPanel() {
		super("prop-explorer", "Prop Explorer");
		this.sortByClosest = new ImBoolean(false);
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.inGame) {
			close();
			return;
		}

		var allProps = new ArrayList<Prop>();
		var clientProps = new ArrayList<Prop>();

		var props = graphics.mc.level.getProps();

		for (var propList : props.propLists.values()) {
			for (var prop : propList) {
				(prop.isClientSideOnly() ? clientProps : allProps).add(prop);
			}
		}

		var delta = graphics.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

		ImGui.checkbox("Sort by Closest", sortByClosest);
		ImGui.checkbox("Hide Outline", ClientProps.HIDE_OUTLINE);

		if (sortByClosest.get()) {
			var cam = graphics.mc.gameRenderer.getMainCamera().getPosition();
			allProps.sort(Comparator.comparingDouble(p -> p.getPos(delta).distanceToSqr(cam)));
			clientProps.sort(Comparator.comparingDouble(p -> p.getPos(delta).distanceToSqr(cam)));
		}

		ImGui.pushItemWidth(-1F);
		ImGui.separator();

		if (!clientProps.isEmpty()) {
			ImGui.text("Client Props: %,d".formatted(allProps.size()));
			content(graphics, clientProps, delta);
			ImGui.separator();
		}

		ImGui.text("Props: %,d".formatted(allProps.size()));
		content(graphics, allProps, delta);
		ImGui.popItemWidth();
	}

	public void content(ImGraphics graphics, List<Prop> allProps, float delta) {
		for (var prop : allProps) {
			ImGui.pushID(prop.id);

			if (graphics.collapsingHeader(prop.toString(), ImGuiTreeNodeFlags.NoTreePushOnOpen)) {
				ClientProps.OPEN_PROPS.add(prop.id);
				prop.imgui(graphics, delta);
			}

			if (ImGui.isItemHovered()) {
				ClientProps.OPEN_PROPS.add(prop.id);
			}

			ImGui.popID();
		}
	}
}
