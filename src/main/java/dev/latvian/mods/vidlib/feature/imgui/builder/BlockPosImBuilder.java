package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.SelectedPosition;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public class BlockPosImBuilder implements ImBuilder<BlockPos>, SelectedPosition.Holder {
	public static final ImBuilderSupplier<BlockPos> SUPPLIER = BlockPosImBuilder::new;

	public BlockPos.MutableBlockPos pos;
	public SelectedPosition selectedPosition;

	public BlockPosImBuilder() {
		this.pos = new BlockPos.MutableBlockPos(0, 0, 0);
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

		ImGui.alignTextToFramePadding();
		ImGui.text("X");
		ImGui.sameLine();

		ImGuiUtils.INT.set(pos.getX());
		ImGui.dragInt("###x", ImGuiUtils.INT.getData(), 1);
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

		ImGui.alignTextToFramePadding();
		ImGui.text("Y");
		ImGui.sameLine();

		ImGuiUtils.INT.set(pos.getY());
		ImGui.dragInt("###y", ImGuiUtils.INT.getData(), 1);
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

		ImGui.alignTextToFramePadding();
		ImGui.text("Y");
		ImGui.sameLine();

		ImGuiUtils.INT.set(pos.getZ());
		ImGui.dragInt("###z", ImGuiUtils.INT.getData(), 1);
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

	@Override
	@Nullable
	public SelectedPosition getSelectedPosition() {
		return selectedPosition;
	}
}
