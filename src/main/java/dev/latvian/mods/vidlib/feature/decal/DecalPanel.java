package dev.latvian.mods.vidlib.feature.decal;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.imgui.AdminPanel;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;

public class DecalPanel extends AdminPanel {
	public static final DecalPanel INSTANCE = new DecalPanel();

	private DecalPanel() {
		super("decals", "Decals");
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.inGame) {
			close();
			return;
		}

		var decals = graphics.mc.player.vl$sessionData().debugDecals;

		ImGui.text("Decals: %,d".formatted(decals.size()));
		ImGui.pushItemWidth(-1F);

		for (int i = 0; i < decals.size(); i++) {
			var decal = decals.get(i);
			ImGui.pushID(i);

			if (graphics.collapsingHeader("#" + (i + 1), ImGuiTreeNodeFlags.NoTreePushOnOpen)) {
				if (graphics.button(ImIcons.REMOVE + " Remove###remove", ImColorVariant.RED)) {
					decals.remove(i);
					ImGui.popID();
					break;
				}

				ImGui.sameLine();

				if (ImGui.button(ImIcons.ADD + " Clone###clone")) {
					decals.add(new Decal(decal));
				}

				decal.imgui(graphics, decals);
			}

			ImGui.popID();
		}

		ImGui.separator();

		if (ImGui.button(ImIcons.ADD + " Add###add")) {
			var d = new Decal();
			d.type = DecalType.CYLINDER;
			d.start = 5F;
			d.end = 6F;
			d.startColor = d.endColor = Color.RED.withAlpha(100);
			d.setPosition(graphics.mc.player.getPosition(graphics.mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)).add(0D, 0.5D, 0D), true);
			decals.add(d);
		}

		ImGui.sameLine();

		if (ImGui.button(ImIcons.ADD + " Add Danger###add-danger")) {
			var pos = graphics.mc.player.getPosition(graphics.mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)).add(0D, -0.0625D, 0D);

			var d = Decal.createDanger(4F);
			d.setPosition(pos, true);
			decals.add(d);
		}

		ImGui.popItemWidth();
	}
}
