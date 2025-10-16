package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.math.Identity;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.SelectedPosition;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Position;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Vector3dImBuilder implements ImBuilder<Vector3d>, SelectedPosition.Holder {
	public static final ImBuilderType<Vector3d> TYPE = Vector3dImBuilder::new;

	public final Vector3d data;
	private final SelectedPosition[] selectedPosition;

	public Vector3dImBuilder() {
		this(Identity.DVEC_3);
	}

	public Vector3dImBuilder(Position position) {
		this.data = new Vector3d(position.x(), position.y(), position.z());
		this.selectedPosition = new SelectedPosition[1];
	}

	public Vector3dImBuilder(Vector3dc position) {
		this.data = new Vector3d(position.x(), position.y(), position.z());
		this.selectedPosition = new SelectedPosition[1];
	}

	@Override
	public void set(Vector3d value) {
		data.set(value.x(), value.y(), value.z());
	}

	public static ImUpdate imgui(ImGraphics graphics, Vector3d data, SelectedPosition[] selectedPosition) {
		selectedPosition[0] = null;
		var update = ImUpdate.NONE;

		if (ImGui.button(SelectedPosition.CAMERA.icon + "###camera-pos")) {
			var cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			data.set(cam.x, cam.y, cam.z);
			update = ImUpdate.FULL;
			selectedPosition[0] = SelectedPosition.CAMERA;
		}

		ImGuiUtils.hoveredTooltip("Use Camera Position");

		ImGui.sameLine();

		ImGui.alignTextToFramePadding();
		ImGui.text("X");
		ImGui.sameLine();

		float dragW = Math.max(ImGui.getContentRegionAvailX(), 100F);

		ImGuiUtils.FLOAT.set((float) data.x);
		ImGui.setNextItemWidth(dragW);
		ImGui.dragFloat("###x", ImGuiUtils.FLOAT.getData(), 0.0625F, -30000000F, 30000000F, "%.4f");
		update = update.orItemEdit();
		data.x = ImGuiUtils.FLOAT.get();

		if (ImGui.button(SelectedPosition.ENTITY.icon + "###entity-pos")) {
			var entity = Minecraft.getInstance().player;

			if (entity != null) {
				data.set(entity.getX(), entity.getY(), entity.getZ());
				update = ImUpdate.FULL;
				selectedPosition[0] = SelectedPosition.ENTITY;
			}
		}

		ImGuiUtils.hoveredTooltip("Use Entity Position");

		ImGui.sameLine();

		ImGui.alignTextToFramePadding();
		ImGui.text("Y");
		ImGui.sameLine();

		ImGuiUtils.FLOAT.set((float) data.y);
		ImGui.setNextItemWidth(dragW);
		ImGui.dragFloat("###y", ImGuiUtils.FLOAT.getData(), 0.0625F, -30000000F, 30000000F, "%.4f");
		update = update.orItemEdit();
		data.y = ImGuiUtils.FLOAT.get();

		if (ImGui.button(SelectedPosition.CURSOR.icon + "###cursor-pos")) {
			var worldMouse = graphics.mc.getWorldMouse();
			var pos = worldMouse == null ? null : worldMouse.clipOutline();

			if (pos != null) {
				data.set(pos.pos().x, pos.pos().y, pos.pos().z);
				update = ImUpdate.FULL;
				selectedPosition[0] = SelectedPosition.CURSOR;
			}
		}

		ImGuiUtils.hoveredTooltip("Use Cursor Position");

		ImGui.sameLine();

		ImGui.alignTextToFramePadding();
		ImGui.text("Z");
		ImGui.sameLine();

		ImGuiUtils.FLOAT.set((float) data.z);
		ImGui.setNextItemWidth(dragW);
		ImGui.dragFloat("###z", ImGuiUtils.FLOAT.getData(), 0.0625F, -30000000F, 30000000F, "%.4f");
		update = update.orItemEdit();
		data.z = ImGuiUtils.FLOAT.get();

		return update;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return imgui(graphics, data, selectedPosition);
	}

	@Override
	public boolean isValid() {
		return data.isFinite();
	}

	@Override
	public Vector3d build() {
		return new Vector3d(data.x, data.y, data.z);
	}

	@Override
	@Nullable
	public SelectedPosition getSelectedPosition() {
		return selectedPosition[0];
	}
}
