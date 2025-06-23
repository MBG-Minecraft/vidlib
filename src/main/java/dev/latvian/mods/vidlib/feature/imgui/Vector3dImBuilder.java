package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.math.KMath;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class Vector3dImBuilder implements ImBuilder<Vec3> {
	public final Vector3d data;

	public Vector3dImBuilder() {
		this.data = new Vector3d();
	}

	public Vector3dImBuilder(Position position) {
		this.data = new Vector3d(position.x(), position.y(), position.z());
	}

	@Override
	public void set(Vec3 value) {
		data.set(value.x(), value.y(), value.z());
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		ImGui.pushItemWidth(-1F);

		if (ImGui.button(ImIcons.CAMERA + "###camera-pos")) {
			var cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			data.set(cam.x, cam.y, cam.z);
			update = ImUpdate.FULL;
		}

		if (ImGui.isItemHovered()) {
			ImGui.setTooltip("Use Camera Position");
		}

		ImGui.sameLine();

		ImGuiUtils.DOUBLE.set(data.x);
		ImGui.inputDouble("###x", ImGuiUtils.DOUBLE, 0.0625D, 1D, "%.3f");
		update = update.orItemEdit();
		data.x = ImGuiUtils.DOUBLE.get();

		if (ImGui.button(ImIcons.ACCOUNT + "###entity-pos")) {
			var entity = Minecraft.getInstance().player;

			if (entity != null) {
				data.set(entity.getX(), entity.getY(), entity.getZ());
				update = ImUpdate.FULL;
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

		if (ImGui.button(ImIcons.TARGET + "###hit-pos")) {
			var pos = Minecraft.getInstance().getWorldMouse().clipOutline();

			if (pos != null) {
				data.set(pos.pos().x, pos.pos().y, pos.pos().z);
				update = ImUpdate.FULL;
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

		ImGui.popItemWidth();
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
