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
	public static final ImBuilderType<BlockPos> TYPE = BlockPosImBuilder::new;

	public BlockPos.MutableBlockPos pos;
	private final SelectedPosition[] selectedPosition;

	public BlockPosImBuilder() {
		this.pos = new BlockPos.MutableBlockPos(0, 0, 0);
		this.selectedPosition = new SelectedPosition[1];
	}

	@Override
	public void set(BlockPos value) {
		pos.set(value);
	}

	public static ImUpdate imgui(ImGraphics graphics, BlockPos.MutableBlockPos data, SelectedPosition[] selectedPosition) {
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

		ImGuiUtils.INT.set(data.getX());
		ImGui.setNextItemWidth(dragW);
		ImGui.dragInt("###x", ImGuiUtils.INT.getData(), 1);
		update = update.orItemEdit();
		data.setX(ImGuiUtils.INT.get());

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

		ImGuiUtils.INT.set(data.getY());
		ImGui.setNextItemWidth(dragW);
		ImGui.dragInt("###y", ImGuiUtils.INT.getData(), 1);
		update = update.orItemEdit();
		data.setY(ImGuiUtils.INT.get());

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
		ImGui.text("Y");
		ImGui.sameLine();

		ImGuiUtils.INT.set(data.getZ());
		ImGui.setNextItemWidth(dragW);
		ImGui.dragInt("###z", ImGuiUtils.INT.getData(), 1);
		update = update.orItemEdit();
		data.setZ(ImGuiUtils.INT.get());
		return update;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return imgui(graphics, pos, selectedPosition);
	}

	@Override
	public boolean keySameLine() {
		return false;
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
		return selectedPosition[0];
	}
}
