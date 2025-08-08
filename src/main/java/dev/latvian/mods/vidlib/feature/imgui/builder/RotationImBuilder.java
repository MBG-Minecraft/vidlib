package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;

public class RotationImBuilder implements ImBuilder<Rotation> {
	public static final ImBuilderType<Rotation> TYPE = RotationImBuilder::new;

	public final float[] value;

	public RotationImBuilder() {
		this.value = new float[]{0F, 0F};
	}

	@Override
	public void set(Rotation v) {
		value[0] = v.yawDeg();
		value[1] = v.pitchDeg();
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		ImGui.dragFloat2("###range", value, 0.25F, -180F, 180F, "%f");
		return ImUpdate.itemEdit();
	}

	@Override
	public Rotation build() {
		return Rotation.deg(value[0], value[1]);
	}

	@Override
	public boolean equals(Rotation a, Rotation b) {
		return a.isSimilar(b);
	}
}
