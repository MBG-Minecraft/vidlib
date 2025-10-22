package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.math.Identity;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.SelectedPosition;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Vec3ImBuilder implements ImBuilder<Vec3>, SelectedPosition.Holder {
	public static final ImBuilderType<Vec3> TYPE = Vec3ImBuilder::new;

	public final Vector3d data;
	private final SelectedPosition[] selectedPosition;

	public Vec3ImBuilder() {
		this(Identity.DVEC_3);
	}

	public Vec3ImBuilder(Position position) {
		this.data = new Vector3d(position.x(), position.y(), position.z());
		this.selectedPosition = new SelectedPosition[1];
	}

	public Vec3ImBuilder(Vector3dc position) {
		this.data = new Vector3d(position.x(), position.y(), position.z());
		this.selectedPosition = new SelectedPosition[1];
	}

	@Override
	public void set(Vec3 value) {
		data.set(value.x(), value.y(), value.z());
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return Vector3dImBuilder.imgui(graphics, data, selectedPosition);
	}

	@Override
	public boolean keySameLine() {
		return false;
	}

	@Override
	public boolean isValid() {
		return !Double.isNaN(data.x) && !Double.isNaN(data.y) && !Double.isNaN(data.z);
	}

	@Override
	public Vec3 build() {
		return KMath.vec3(data.x, data.y, data.z);
	}

	@Override
	@Nullable
	public SelectedPosition getSelectedPosition() {
		return selectedPosition[0];
	}
}
