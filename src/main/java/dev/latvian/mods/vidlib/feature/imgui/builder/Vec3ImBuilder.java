package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.SelectedPosition;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Vec3ImBuilder implements ImBuilder<Vec3> {
	public static final ImBuilderSupplier<Vec3> SUPPLIER = Vec3ImBuilder::new;

	public final Vector3d data;
	public SelectedPosition selectedPosition;

	public Vec3ImBuilder() {
		this.data = new Vector3d();
	}

	public Vec3ImBuilder(Position position) {
		this.data = new Vector3d(position.x(), position.y(), position.z());
	}

	public Vec3ImBuilder(Vector3dc position) {
		this.data = new Vector3d(position.x(), position.y(), position.z());
	}

	@Override
	public void set(Vec3 value) {
		data.set(value.x(), value.y(), value.z());
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		selectedPosition = null;
		var update = ImUpdate.NONE;

		if (ImGui.button(SelectedPosition.CAMERA.icon + "###camera-pos")) {
			var cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			data.set(cam.x, cam.y, cam.z);
			update = ImUpdate.FULL;
			selectedPosition = SelectedPosition.CAMERA;
		}

		if (ImGui.isItemHovered()) {
			ImGui.setTooltip("Use Camera Position");
		}

		ImGui.sameLine();

		ImGuiUtils.DOUBLE.set(data.x);
		ImGui.inputDouble("###x", ImGuiUtils.DOUBLE, 0.0625D, 1D, "%.3f");
		update = update.orItemEdit();
		data.x = ImGuiUtils.DOUBLE.get();

		if (ImGui.button(SelectedPosition.ENTITY.icon + "###entity-pos")) {
			var entity = Minecraft.getInstance().player;

			if (entity != null) {
				data.set(entity.getX(), entity.getY(), entity.getZ());
				update = ImUpdate.FULL;
				selectedPosition = SelectedPosition.ENTITY;
			}
		}

		if (ImGui.isItemHovered()) {
			ImGui.setTooltip("Use Entity Position");
		}

		ImGui.sameLine();

		ImGuiUtils.DOUBLE.set(data.y);
		ImGui.inputDouble("###y", ImGuiUtils.DOUBLE, 0.0625D, 1D, "%.3f");
		update = update.orItemEdit();
		data.y = ImGuiUtils.DOUBLE.get();

		if (ImGui.button(SelectedPosition.CURSOR.icon + "###cursor-pos")) {
			var worldMouse = graphics.mc.getWorldMouse();
			var pos = worldMouse == null ? null : worldMouse.clipOutline();

			if (pos != null) {
				data.set(pos.pos().x, pos.pos().y, pos.pos().z);
				update = ImUpdate.FULL;
				selectedPosition = SelectedPosition.CURSOR;
			}
		}

		if (ImGui.isItemHovered()) {
			ImGui.setTooltip("Use Cursor Position");
		}

		ImGui.sameLine();

		ImGuiUtils.DOUBLE.set(data.z);
		ImGui.inputDouble("###z", ImGuiUtils.DOUBLE, 0.0625D, 1D, "%.3f");
		update = update.orItemEdit();
		data.z = ImGuiUtils.DOUBLE.get();

		return update;
	}

	@Override
	public boolean isValid() {
		return !Double.isNaN(data.x) && !Double.isNaN(data.y) && !Double.isNaN(data.z);
	}

	@Override
	public Vec3 build() {
		return KMath.vec3(data.x, data.y, data.z);
	}
}
