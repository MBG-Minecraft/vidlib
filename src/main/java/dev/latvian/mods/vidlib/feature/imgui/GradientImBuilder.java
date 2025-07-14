package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.CompoundGradient;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.color.PositionedColor;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.List;

public class GradientImBuilder implements ImBuilder<Gradient> {
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

		for (int i = 0; i < colors.size(); i++) {
			if (i > 0) {
				ImGui.sameLine();
			}

			var c = colors.get(i);

			ImGui.pushID(i);
			graphics.pushStack();
			graphics.setButtonColor(c.color.build());

			if (ImGui.button("###open-popup", 30F, 30F)) {
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

		if (ImGui.button(ImIcons.ADD + "###add", 30F, 30F)) {
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
