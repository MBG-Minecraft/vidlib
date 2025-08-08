package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

public class PropExplorerPanel extends AdminPanel {
	public static final PropExplorerPanel INSTANCE = new PropExplorerPanel();

	public static final IntSet OPEN_PROPS = new IntOpenHashSet();
	public static final IntSet HIDDEN_PROPS = new IntOpenHashSet();
	public static final Set<PropType<?>> HIDDEN_PROP_TYPES = new ReferenceOpenHashSet<>();

	public static boolean isPropHidden(Prop prop) {
		return HIDDEN_PROPS.contains(prop.id) || HIDDEN_PROP_TYPES.contains(prop.type);
	}

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

			if (ImGui.collapsingHeader(prop.toString(), ImGuiTreeNodeFlags.NoTreePushOnOpen)) {
				OPEN_PROPS.add(prop.id);
				prop.imgui(graphics, delta);
			}

			ImGui.popID();
		}

		ImGui.popItemWidth();
	}
}
