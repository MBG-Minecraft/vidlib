package dev.latvian.mods.vidlib.feature.prop.builtin.structure;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ResourceLocationImBuilder;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.structure.StructureHolder;
import dev.latvian.mods.vidlib.feature.structure.StructureRenderer;
import dev.latvian.mods.vidlib.feature.structure.StructureRendererData;
import dev.latvian.mods.vidlib.feature.structure.StructureStorage;
import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

public class StructureProp extends Prop {
	public static final PropData<StructureProp, Identifier> STRUCTURE = PropData.create(StructureProp.class, "structure", ID.DATA_TYPE, p -> p.structure, (p, v) -> p.structure = v);
	public static final PropData<StructureProp, Boolean> SOLID = PropData.createBoolean(StructureProp.class, "solid", p -> p.solid, (p, v) -> p.solid = v);
	public static final PropData<StructureProp, Boolean> CENTER_X = PropData.createBoolean(StructureProp.class, "center_x", p -> p.centerX, (p, v) -> p.centerX = v);
	public static final PropData<StructureProp, Boolean> CENTER_Y = PropData.createBoolean(StructureProp.class, "center_y", p -> p.centerY, (p, v) -> p.centerY = v);
	public static final PropData<StructureProp, Boolean> CENTER_Z = PropData.createBoolean(StructureProp.class, "center_z", p -> p.centerZ, (p, v) -> p.centerZ = v);
	public static final PropData<StructureProp, Boolean> CULL = PropData.createBoolean(StructureProp.class, "cull", p -> p.cull, (p, v) -> p.cull = v);
	public static final PropData<StructureProp, Integer> SKY_LIGHT = PropData.createInt(StructureProp.class, "sky_light", p -> p.skyLight, (p, v) -> p.skyLight = v, 0, 15);
	public static final PropData<StructureProp, Integer> BLOCK_LIGHT = PropData.createInt(StructureProp.class, "block_light", p -> p.blockLight, (p, v) -> p.blockLight = v, 0, 15);

	@AutoRegister
	public static final PropType<StructureProp> TYPE = PropType.create(ID.vidlib("structure"), StructureProp::new,
		TICK,
		LIFESPAN,
		POSITION,
		YAW,
		PITCH,
		ROLL,
		WIDTH,
		HEIGHT,
		CAN_COLLIDE,
		CAN_INTERACT,
		STRUCTURE,
		SOLID,
		CENTER_X,
		CENTER_Y,
		CENTER_Z,
		CULL,
		SKY_LIGHT,
		BLOCK_LIGHT
	);

	private static final Map<Identifier, StructureRenderer> RENDERERS = new HashMap<>();
	private final ResourceLocationImBuilder structureBuilder = new ResourceLocationImBuilder(true);
	public Identifier structure;
	public boolean solid;
	public boolean centerX;
	public boolean centerY;
	public boolean centerZ;
	public boolean cull;
	public int skyLight;
	public int blockLight;

	@Nullable
	private Identifier builderValue;

	public StructureProp(PropContext<?> ctx) {
		super(ctx);
		this.structure = ID.EMPTY;
		this.solid = false;
		this.centerX = true;
		this.centerY = false;
		this.centerZ = true;
		this.cull = true;
		this.skyLight = 15;
		this.blockLight = 15;
	}

	@Nullable
	public static StructureRenderer getRenderer(@Nullable Identifier structureId) {
		if (structureId == null || structureId.equals(ID.EMPTY)) {
			return null;
		}

		return RENDERERS.computeIfAbsent(structureId, id -> StructureRenderer.create(id, id));
	}

	@Nullable
	public StructureRenderer getRenderer() {
		return getRenderer(structure);
	}

	@Nullable
	public StructureHolder getHolder() {
		var renderer = getRenderer();
		var holder = renderer == null ? null : renderer.getStructure();
		return holder == null || holder.empty() ? null : holder;
	}

	public StructureRendererData createRendererData() {
		return new StructureRendererData(centerX, centerY, centerZ, cull, Color.TRANSPARENT, skyLight, blockLight, false);
	}

	@Override
	public boolean isVisibleInFrustum(double x, double y, double z, FrustumCheck frustum) {
		return true;
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

			if (!structure.equals(builderValue)) {
				builderValue = structure;
				structureBuilder.set(structure);
			}

			var update = structureBuilder.imgui(graphics);
			ImGui.popItemWidth();

			if (update.isAny() && structureBuilder.isValid()) {
				c2sEdit(STRUCTURE, structureBuilder.build(), update.isFull());
			}

			var keys = StructureStorage.CLIENT.keySet();

			if (!keys.isEmpty()) {
				ImGui.tableNextRow();
				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();
				ImGui.textDisabled("available");
				ImGui.tableNextColumn();
				ImGui.pushItemWidth(-1F);
				var current = structure.toString();

				if (ImGui.beginCombo("###available", current)) {
					for (var key : keys) {
						if (ImGui.selectable(key, key.equals(current))) {
							c2sEdit(STRUCTURE, Identifier.parse(key), true);
						}
					}

					ImGui.endCombo();
				}

				ImGui.popItemWidth();
			}

			booleanRow("solid", SOLID, solid);
			booleanRow("center_x", CENTER_X, centerX);
			booleanRow("center_y", CENTER_Y, centerY);
			booleanRow("center_z", CENTER_Z, centerZ);
			booleanRow("cull", CULL, cull);
			positionRow("x", 0);
			positionRow("y", 1);
			positionRow("z", 2);
			lightRow("sky_light", SKY_LIGHT, skyLight);
			lightRow("block_light", BLOCK_LIGHT, blockLight);

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
		ImGuiUtils.FLOAT.set((float) (axis == 0 ? pos.x : axis == 1 ? pos.y : pos.z));
		ImGui.dragFloat("###pos_" + label, ImGuiUtils.FLOAT.getData(), 0.0625F, -30000000F, 30000000F, "%.4f");
		var update = ImUpdate.itemEdit();
		ImGui.popItemWidth();

		if (update.isAny()) {
			double x = axis == 0 ? ImGuiUtils.FLOAT.get() : pos.x;
			double y = axis == 1 ? ImGuiUtils.FLOAT.get() : pos.y;
			double z = axis == 2 ? ImGuiUtils.FLOAT.get() : pos.z;
			c2sEdit(POSITION, new Vector3d(x, y, z), update.isFull());
		}
	}

	private void booleanRow(String label, PropData<StructureProp, Boolean> data, boolean value) {
		ImGui.tableNextRow();
		ImGui.tableNextColumn();
		ImGui.alignTextToFramePadding();
		ImGui.text(label);
		ImGui.tableNextColumn();

		if (ImGui.checkbox("###" + label, value)) {
			c2sEdit(data, !value, true);
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
			c2sEdit(data, ImGuiUtils.INT.get(), true);
		}

		ImGui.popItemWidth();
	}
}