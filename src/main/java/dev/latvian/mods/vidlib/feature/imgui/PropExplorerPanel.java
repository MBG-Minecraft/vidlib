package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.prop.Prop;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.Comparator;

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

		var props = graphics.mc.level.getProps();

		for (var propList : props.propLists.values()) {
			for (var prop : propList) {
				allProps.add(prop);
			}
		}

		var delta = graphics.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

		ImGui.text("Props: %,d".formatted(allProps.size()));
		ImGui.checkbox("Sort by Closest", sortByClosest);

		if (sortByClosest.get() && allProps.size() >= 2) {
			var cam = graphics.mc.gameRenderer.getMainCamera().getPosition();
			allProps.sort(Comparator.comparingDouble(p -> p.getPos(delta).distanceToSqr(cam)));
		}

		ImGui.pushItemWidth(-1F);

		for (var prop : allProps) {
			ImGui.pushID(prop.id);

			if (ImGui.collapsingHeader(prop.toString(), ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.DefaultOpen)) {
				prop.imgui(graphics, delta);
			}

			ImGui.popID();
		}

		ImGui.popItemWidth();
	}
}
