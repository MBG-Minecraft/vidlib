package dev.latvian.mods.vidlib.feature.decal;

import dev.latvian.mods.klib.util.ColorBlendMode;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.SelectedPosition;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color4ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.Vector3dImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.flag.ImGuiSliderFlags;

import java.util.Collection;

public class DecalUI {
	public static void imgui(Decal decal, ImGraphics graphics, Collection<Decal> decals) {
		ImGui.text("Shape");
		DecalType.UNIT[0] = decal.type;
		graphics.combo("###type", DecalType.UNIT, "", DecalType.VALUES);
		decal.type = DecalType.UNIT[0];

		if (decal.type == DecalType.REGULAR) {
			ImGuiUtils.FLOAT.set(decal.edges);
			ImGui.sliderFloat("###edges", ImGuiUtils.FLOAT.getData(), 1F, 16F, "%.01f");
			decal.edges = ImGuiUtils.FLOAT.get();
			graphics.hoveredTooltip("Edges");
		}

		if (decal.parent != null && !decals.contains(decal.parent)) {
			decal.setPosition(decal.position, false);
		}

		if (decal.parent != null) {
			ImGuiUtils.BOOLEAN.set(true);

			if (ImGui.checkbox(ImIcons.LOCK + " Joined Position###joined-position", ImGuiUtils.BOOLEAN)) {
				decal.setPosition(decal.position, false);
			}
		} else {
			ImGui.text("Position");
			Vector3dImBuilder.imgui(graphics, decal.position, SelectedPosition.UNIT);
		}

		ImGuiUtils.BOOLEAN.set(decal.terrain);

		if (ImGui.checkbox("Terrain###terrain", ImGuiUtils.BOOLEAN)) {
			decal.terrain = ImGuiUtils.BOOLEAN.get();
		}

		ImGui.text("Blend Mode");
		var blendModeArr = new ColorBlendMode[]{decal.blendMode};
		graphics.combo("###blend-mode", blendModeArr, "", ColorBlendMode.VALUES, c -> c.displayName);
		decal.blendMode = blendModeArr[0];

		ImGui.text("Size");
		graphics.smallText("Width");

		float[] innerArr = {decal.innerSize};
		float[] outerArr = {decal.outerSize};
		ImGuiUtils.FLOAT.set(decal.innerSize);
		ImGui.dragFloatRange2("###size-range", innerArr, outerArr, 0.0625F, 0F, 100F, "%f");
		decal.innerSize = innerArr[0];
		decal.outerSize = outerArr[0];

		graphics.hoveredTooltip("Size Range");

		float diff = decal.outerSize - decal.innerSize;
		if (ImGui.dragFloat("###size", innerArr, 0.0625F, 0F, 100F, "%f")) {
			decal.innerSize = innerArr[0];
			decal.outerSize = decal.innerSize + diff;
		}

		graphics.hoveredTooltip("Size");

		graphics.smallText("Height");

		ImGuiUtils.FLOAT.set(decal.heightScale);
		ImGui.sliderFloat("###height-scale", ImGuiUtils.FLOAT.getData(), 0.125F, 8F, "%f", ImGuiSliderFlags.Logarithmic);
		decal.heightScale = ImGuiUtils.FLOAT.get();

		graphics.hoveredTooltip("Height Scale");

		innerArr[0] = decal.getHeight();

		if (ImGui.dragFloat("###height", innerArr, 0.0625F, 0F, 100F, "%f")) {
			decal.setHeight(innerArr[0]);
		}

		graphics.hoveredTooltip("Height");

		// start = starta[0];
		// end = start + hdiff;

		Color4ImBuilder.UNIT.set(decal.innerColor);
		Color4ImBuilder.UNIT.imguiKey(graphics, "Inner Color", "inner-color");
		decal.innerColor = Color4ImBuilder.UNIT.build();

		Color4ImBuilder.UNIT.set(decal.outerColor);
		Color4ImBuilder.UNIT.imguiKey(graphics, "Outer Color", "outer-color");
		decal.outerColor = Color4ImBuilder.UNIT.build();

		ImGui.alignTextToFramePadding();
		ImGui.text("Fill");
		ImGui.sameLine();

		DecalFillType.UNIT[0] = decal.fillType;
		graphics.combo("###fill-type", DecalFillType.UNIT, "", DecalFillType.VALUES, t -> t.displayName);
		decal.fillType = DecalFillType.UNIT[0];

		if (decal.fillType != DecalFillType.SOLID) {
			ImGui.alignTextToFramePadding();
			graphics.smallText("Size");
			ImGui.sameLine();
			ImGuiUtils.FLOAT.set(decal.fillSize);
			ImGui.sliderFloat("###fill-size", ImGuiUtils.FLOAT.getData(), 0F, 4F, "%f");
			decal.fillSize = ImGuiUtils.FLOAT.get();

			ImGui.alignTextToFramePadding();
			graphics.smallText("Thickness");
			ImGui.sameLine();
			ImGuiUtils.FLOAT.set(decal.fillThickness);
			ImGui.sliderFloat("###fill-thickness", ImGuiUtils.FLOAT.getData(), 0F, 0.5F, "%f");
			decal.fillThickness = ImGuiUtils.FLOAT.get();
		}

		ImGui.alignTextToFramePadding();
		ImGui.text("Rotation");
		ImGui.sameLine();
		ImGuiUtils.FLOAT.set(decal.rotation);
		ImGui.sliderFloat("###rotation", ImGuiUtils.FLOAT.getData(), -180F, 180F, "%f");
		decal.rotation = ImGuiUtils.FLOAT.get();
	}
}
