package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Vector3fImBuilder implements ImBuilder<Vector3f> {
	public final float[] data;

	public Vector3fImBuilder() {
		this.data = new float[3];
	}

	public Vector3fImBuilder(Vector3fc position) {
		this.data = new float[]{position.x(), position.y(), position.z()};
	}

	@Override
	public void set(Vector3f value) {
		data[0] = value.x();
		data[1] = value.y();
		data[2] = value.z();
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		ImGui.inputFloat3("###vec", data);
		update = update.orItemEdit();
		return update;
	}

	@Override
	public boolean isValid() {
		return !Double.isNaN(data[0]) && !Double.isNaN(data[1]) && !Double.isNaN(data[2]);
	}

	@Override
	public Vector3f build() {
		return new Vector3f(data[0], data[1], data[2]);
	}
}
