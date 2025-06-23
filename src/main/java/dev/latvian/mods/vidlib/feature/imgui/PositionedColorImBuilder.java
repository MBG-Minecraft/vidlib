package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.color.PositionedColor;
import dev.latvian.mods.klib.easing.Easing;
import imgui.ImGui;
import imgui.type.ImFloat;
import org.jetbrains.annotations.NotNull;

public class PositionedColorImBuilder implements Comparable<PositionedColorImBuilder>, ImBuilder<PositionedColor> {
	public String label;
	public final ImFloat position = new ImFloat(0F);
	public final ColorImBuilder color = new ColorImBuilder("Color###color");
	public final Easing[] easing = {Easing.LINEAR};
	public boolean delete = false;

	public PositionedColorImBuilder(String label) {
		this.label = label;
	}

	@Override
	public void set(PositionedColor c) {
		position.set(c.position());
		color.set(c.color());
		easing[0] = c.easing();
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		delete = false;
		var update = ImUpdate.NONE;

		if (!label.isEmpty()) {
			ImGui.text(label);
			ImGui.sameLine();
		}

		graphics.pushStack();
		graphics.setRedButton();

		if (ImGui.smallButton(ImIcons.DELETE + " Delete")) {
			update = ImUpdate.FULL;
			delete = true;
		}

		graphics.popStack();

		ImGui.sliderFloat("Position###position", position.getData(), 0F, 1F, "%.3f");
		update = update.or(ImUpdate.itemEdit());
		update = update.or(color.imgui(graphics));
		update = update.or(graphics.easingCombo("Easing###easing", easing));

		return update;
	}

	@Override
	public boolean isValid() {
		return color.isValid();
	}

	@Override
	public PositionedColor build() {
		return new PositionedColor(position.get(), color.build(), easing[0]);
	}

	@Override
	public int compareTo(@NotNull PositionedColorImBuilder other) {
		return Float.compare(position.get(), other.position.get());
	}
}
