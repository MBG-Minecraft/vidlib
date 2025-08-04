package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Vector3fImBuilder implements ImBuilder<Vector3f> {
	public static final ImBuilderSupplier<Vector3f> SUPPLIER = Vector3fImBuilder::new;

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
		ImGui.dragFloat3("###vec", data, 0.0625F, -30000000F, 30000000F, "%.4f");
		update = update.orItemEdit();
		return update;
	}

	@Override
	public boolean isValid() {
		return Float.isFinite(data[0]) && Float.isFinite(data[1]) && Float.isFinite(data[2]);
	}

	@Override
	public Vector3f build() {
		return new Vector3f(data[0], data[1], data[2]);
	}
}
