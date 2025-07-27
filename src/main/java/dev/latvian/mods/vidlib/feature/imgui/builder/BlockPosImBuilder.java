package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.SelectedPosition;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public class BlockPosImBuilder implements ImBuilder<BlockPos> {
	public BlockPos.MutableBlockPos pos;
	public SelectedPosition selectedPosition;

	public BlockPosImBuilder() {
		this.pos = new BlockPos.MutableBlockPos(0, 0, 0);
	}

	public BlockPosImBuilder(Vec3i position) {
		this.pos = new BlockPos.MutableBlockPos(position.getX(), position.getY(), position.getZ());
	}

	@Override
	public void set(BlockPos value) {
		pos.set(value);
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		selectedPosition = null;
		var update = ImUpdate.NONE;

		if (ImGui.button(SelectedPosition.CAMERA.icon + "###camera-pos")) {
			var cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			pos.set(cam.x, cam.y, cam.z);
			update = ImUpdate.FULL;
			selectedPosition = SelectedPosition.CAMERA;
		}

		if (ImGui.isItemHovered()) {
			ImGui.setTooltip("Use Camera Position");
		}

		ImGui.sameLine();

		ImGuiUtils.INT.set(pos.getX());
		ImGui.inputInt("###x", ImGuiUtils.INT, 1, 16);
		update = update.orItemEdit();
		pos.setX(ImGuiUtils.INT.get());

		if (ImGui.button(SelectedPosition.ENTITY.icon + "###entity-pos")) {
			var entity = Minecraft.getInstance().player;

			if (entity != null) {
				pos.set(entity.getX(), entity.getY(), entity.getZ());
				update = ImUpdate.FULL;
				selectedPosition = SelectedPosition.ENTITY;
			}
		}

		if (ImGui.isItemHovered()) {
			ImGui.setTooltip("Use Entity Position");
		}

		ImGui.sameLine();

		ImGuiUtils.INT.set(pos.getY());
		ImGui.inputInt("###y", ImGuiUtils.INT, 1, 16);
		update = update.orItemEdit();
		pos.setY(ImGuiUtils.INT.get());

		if (ImGui.button(SelectedPosition.CURSOR.icon + "###cursor-pos")) {
			var worldMouse = graphics.mc.getWorldMouse();
			var pos = worldMouse == null ? null : worldMouse.clipOutline();

			if (pos != null) {
				this.pos.set(pos.pos().x, pos.pos().y, pos.pos().z);
				update = ImUpdate.FULL;
				selectedPosition = SelectedPosition.CURSOR;
			}
		}

		if (ImGui.isItemHovered()) {
			ImGui.setTooltip("Use Cursor Position");
		}

		ImGui.sameLine();

		ImGuiUtils.INT.set(pos.getZ());
		ImGui.inputInt("###z", ImGuiUtils.INT, 1, 16);
		update = update.orItemEdit();
		pos.setZ(ImGuiUtils.INT.get());
		return update;
	}

	@Override
	public boolean isValid() {
		var mc = Minecraft.getInstance();
		return mc.level == null || mc.level.isInWorldBounds(pos);
	}

	@Override
	public BlockPos build() {
		return pos.immutable();
	}
}
