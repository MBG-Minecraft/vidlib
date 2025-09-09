package dev.latvian.mods.vidlib.feature.decal;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.imgui.AdminPanel;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.SelectedPosition;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color4ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.Vector3dImBuilder;
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

		var decals = graphics.mc.player.vl$sessionData().decals;

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
					var d = new Decal();
					d.type = decal.type;
					d.position = decal.position;
					d.start = decal.start;
					d.end = decal.end;
					d.thickness = decal.thickness;
					d.rotation = decal.rotation;
					d.startColor = decal.startColor;
					d.endColor = decal.endColor;
					d.surface = decal.surface;
					d.grid = decal.grid;
					decals.add(d);
				}

				DecalType.UNIT[0] = decal.type;
				graphics.combo("###type", "", DecalType.UNIT, DecalType.VALUES);
				decal.type = DecalType.UNIT[0];

				Vector3dImBuilder.imgui(graphics, decal.position, SelectedPosition.UNIT);

				ImGui.text("Size");

				float[] start = {decal.start};
				float[] end = {decal.end};
				ImGuiUtils.FLOAT.set(decal.start);
				ImGui.dragFloatRange2("###size-range", start, end, 0.0625F, 0F, 100F, "%f");
				decal.start = start[0];
				decal.end = end[0];

				float diff = decal.end - decal.start;
				ImGui.dragFloat("###size", start, 0.0625F, 0F, 100F, "%f");
				decal.start = start[0];
				decal.end = decal.start + diff;

				Color4ImBuilder.UNIT.set(decal.startColor);
				Color4ImBuilder.UNIT.imguiKey(graphics, "Start Color", "start-color");
				decal.startColor = Color4ImBuilder.UNIT.build();

				Color4ImBuilder.UNIT.set(decal.endColor);
				Color4ImBuilder.UNIT.imguiKey(graphics, "End Color", "end-color");
				decal.endColor = Color4ImBuilder.UNIT.build();

				ImGui.text("Grid");

				ImGuiUtils.FLOAT.set(decal.grid);
				ImGui.sliderFloat("###grid", ImGuiUtils.FLOAT.getData(), 0F, 4F, "%f");
				decal.grid = ImGuiUtils.FLOAT.get();

				if (decal.grid > 0F) {
					ImGuiUtils.FLOAT.set(decal.thickness);
					ImGui.sliderFloat("###thickness", ImGuiUtils.FLOAT.getData(), 0F, 0.5F, "%f");
					decal.thickness = ImGuiUtils.FLOAT.get();
				}
			}

			ImGui.popID();
		}

		ImGui.separator();

		if (ImGui.button(ImIcons.ADD + " Add###add")) {
			var d = new Decal();
			d.start = 5F;
			d.end = 6F;
			d.startColor = d.endColor = Color.RED.withAlpha(100);
			d.setPosition(graphics.mc.player.getPosition(graphics.mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)));
			decals.add(d);
		}

		ImGui.popItemWidth();
	}
}
