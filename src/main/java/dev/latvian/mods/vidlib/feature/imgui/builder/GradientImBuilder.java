package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.CompoundGradient;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.color.PositionedColor;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiStyleVar;

import java.util.ArrayList;
import java.util.List;

public class GradientImBuilder implements ImBuilder<Gradient> {
	public static final ImBuilderType<Gradient> TYPE = GradientImBuilder::new;

	public final List<PositionedColorImBuilder> colors;

	public GradientImBuilder() {
		this.colors = new ArrayList<>();
	}

	public GradientImBuilder(List<PositionedColor> colors) {
		this.colors = new ArrayList<>(colors.size());

		for (int i = 0; i < colors.size(); i++) {
			var col = colors.get(i);
			var builder = new PositionedColorImBuilder("Color #" + (i + 1));
			builder.set(col);
			this.colors.add(builder);
		}
	}

	@Override
	public void set(Gradient value) {
		colors.clear();

		var positionedColors = value.getPositionedColors();

		for (int i = 0; i < positionedColors.size(); i++) {
			var col = positionedColors.get(i);
			var builder = new PositionedColorImBuilder("Color #" + (i + 1));
			builder.set(col);
			colors.add(builder);
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		graphics.pushStack();
		graphics.setStyleVar(ImGuiStyleVar.ItemSpacing, 6F, 8F);
		float[] rgba = new float[4];

		for (int i = 0; i < colors.size(); i++) {
			if (i > 0) {
				ImGui.sameLine();
			}

			var c = colors.get(i);

			ImGui.pushID(i);
			graphics.pushStack();
			graphics.setButtonColor(c.color.build());

			rgba[0] = c.color.build().redf();
			rgba[1] = c.color.build().greenf();
			rgba[2] = c.color.build().bluef();
			rgba[3] = c.color.build().alphaf();
			ImGui.colorEdit4("###open-popup", rgba, ImGuiColorEditFlags.NoLabel | ImGuiColorEditFlags.AlphaPreviewHalf | ImGuiColorEditFlags.NoPicker | ImGuiColorEditFlags.NoInputs);

			if (ImGui.isItemClicked()) {
				ImGui.openPopup("###popup");
			}

			graphics.popStack();

			if (ImGui.beginPopup("###popup")) {
				update = update.or(c.imgui(graphics));
				ImGui.endPopup();
			}

			ImGui.popID();
		}

		if (colors.removeIf(c -> c.delete)) {
			update = ImUpdate.FULL;

			for (int i = 0; i < colors.size(); i++) {
				colors.get(i).label = "Color #" + (i + 1);
			}
		}

		if (!colors.isEmpty()) {
			ImGui.sameLine();
		}

		if (graphics.button(ImIcons.ADD + "###add", ImColorVariant.GREEN)) {
			if (colors.size() == 1) {
				colors.getFirst().position.set(0F);
			} else {
				float shift = colors.size() / (colors.size() + 1F);

				for (var c : colors) {
					c.position.set(c.position.get() * shift);
				}
			}

			var builder = new PositionedColorImBuilder("Color #" + (colors.size() + 1));
			builder.set(new PositionedColor(1F, Color.WHITE));
			colors.add(builder);
			update = ImUpdate.FULL;
		}

		ImGuiUtils.hoveredTooltip("Add");

		graphics.popStack();

		return update;
	}

	@Override
	public boolean isValid() {
		if (colors.isEmpty()) {
			return false;
		}

		for (var c : colors) {
			if (!c.isValid()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public Gradient build() {
		var list = new ArrayList<PositionedColor>();

		for (var c : colors) {
			list.add(c.build());
		}

		return new CompoundGradient(list).optimize();
	}
}
