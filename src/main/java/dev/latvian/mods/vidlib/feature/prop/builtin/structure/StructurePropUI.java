package dev.latvian.mods.vidlib.feature.prop.builtin.structure;

import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ResourceLocationImBuilder;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropUI;
import dev.latvian.mods.vidlib.feature.structure.StructureStorage;
import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class StructurePropUI extends PropUI<StructureProp> {
	@ClientAutoRegister
	public static final Factory<StructureProp> FACTORY = new Factory<>(StructureProp.TYPE, StructurePropUI::new);

	private final ResourceLocationImBuilder structureBuilder = new ResourceLocationImBuilder(true);

	@Nullable
	private Identifier builderValue;

	public StructurePropUI(StructureProp prop) {
		super(prop);
	}

	@Override
	public void imgui(ImGraphics graphics, float delta) {
		super.imgui(graphics, delta);

		if (ImGui.beginTable("###structure_data", 2, ImGuiTableFlags.SizingStretchProp | ImGuiTableFlags.Borders)) {
			ImGui.tableNextRow();
			ImGui.tableNextColumn();
			ImGui.alignTextToFramePadding();
			ImGui.text("structure");
			ImGui.tableNextColumn();
			ImGui.pushItemWidth(-1F);

			if (!prop.structure.equals(builderValue)) {
				builderValue = prop.structure;
				structureBuilder.set(prop.structure);
			}

			var update = structureBuilder.imgui(graphics);
			ImGui.popItemWidth();

			if (update.isAny() && structureBuilder.isValid()) {
				prop.c2sEdit(StructureProp.STRUCTURE, structureBuilder.build(), update.isFull());
			}

			var keys = StructureStorage.CLIENT.keySet();

			if (!keys.isEmpty()) {
				ImGui.tableNextRow();
				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();
				ImGui.textDisabled("available");
				ImGui.tableNextColumn();
				ImGui.pushItemWidth(-1F);
				var current = prop.structure.toString();

				if (ImGui.beginCombo("###available", current)) {
					for (var key : keys) {
						if (ImGui.selectable(key, key.equals(current))) {
							prop.c2sEdit(StructureProp.STRUCTURE, Identifier.parse(key), true);
						}
					}

					ImGui.endCombo();
				}

				ImGui.popItemWidth();
			}

			booleanRow("solid", StructureProp.SOLID, prop.solid);
			booleanRow("center_x", StructureProp.CENTER_X, prop.centerX);
			booleanRow("center_y", StructureProp.CENTER_Y, prop.centerY);
			booleanRow("center_z", StructureProp.CENTER_Z, prop.centerZ);
			booleanRow("cull", StructureProp.CULL, prop.cull);
			positionRow("x", 0);
			positionRow("y", 1);
			positionRow("z", 2);
			lightRow("sky_light", StructureProp.SKY_LIGHT, prop.skyLight);
			lightRow("block_light", StructureProp.BLOCK_LIGHT, prop.blockLight);

			ImGui.endTable();
		}
	}

	private void positionRow(String label, int axis) {
		ImGui.tableNextRow();
		ImGui.tableNextColumn();
		ImGui.alignTextToFramePadding();
		ImGui.text(label);
		ImGui.tableNextColumn();
		ImGui.pushItemWidth(-1F);
		ImGuiUtils.FLOAT.set((float) (axis == 0 ? prop.pos.x : axis == 1 ? prop.pos.y : prop.pos.z));
		ImGui.dragFloat("###pos_" + label, ImGuiUtils.FLOAT.getData(), 0.0625F, -30000000F, 30000000F, "%.4f");
		var update = ImUpdate.itemEdit();
		ImGui.popItemWidth();

		if (update.isAny()) {
			double x = axis == 0 ? ImGuiUtils.FLOAT.get() : prop.pos.x;
			double y = axis == 1 ? ImGuiUtils.FLOAT.get() : prop.pos.y;
			double z = axis == 2 ? ImGuiUtils.FLOAT.get() : prop.pos.z;
			prop.c2sEdit(Prop.POSITION, new Vector3d(x, y, z), update.isFull());
		}
	}

	private void booleanRow(String label, PropData<StructureProp, Boolean> data, boolean value) {
		ImGui.tableNextRow();
		ImGui.tableNextColumn();
		ImGui.alignTextToFramePadding();
		ImGui.text(label);
		ImGui.tableNextColumn();

		if (ImGui.checkbox("###" + label, value)) {
			prop.c2sEdit(data, !value, true);
		}
	}

	private void lightRow(String label, PropData<StructureProp, Integer> data, int value) {
		ImGui.tableNextRow();
		ImGui.tableNextColumn();
		ImGui.alignTextToFramePadding();
		ImGui.text(label);
		ImGui.tableNextColumn();
		ImGui.pushItemWidth(-1F);
		ImGuiUtils.INT.set(value);
		ImGui.sliderInt("###" + label, ImGuiUtils.INT.getData(), 0, 15);

		if (ImGuiUtils.INT.get() != value) {
			prop.c2sEdit(data, ImGuiUtils.INT.get(), true);
		}

		ImGui.popItemWidth();
	}
}
