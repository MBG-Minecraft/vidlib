package dev.latvian.mods.vidlib.feature.prop.builtin.structure;

import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.structure.StructureHolder;
import dev.latvian.mods.vidlib.feature.structure.StructureRendererData;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class StructureProp extends Prop {
	public static final PropData<StructureProp, Identifier> STRUCTURE = PropData.create(StructureProp.class, "structure", ID.DATA_TYPE, p -> p.structure, StructureProp::setStructure);
	public static final PropData<StructureProp, Boolean> SOLID = PropData.createBoolean(StructureProp.class, "solid", p -> p.solid, (p, v) -> p.solid = v);
	public static final PropData<StructureProp, Boolean> CENTER_X = PropData.createBoolean(StructureProp.class, "center_x", p -> p.centerX, (p, v) -> p.centerX = v);
	public static final PropData<StructureProp, Boolean> CENTER_Y = PropData.createBoolean(StructureProp.class, "center_y", p -> p.centerY, (p, v) -> p.centerY = v);
	public static final PropData<StructureProp, Boolean> CENTER_Z = PropData.createBoolean(StructureProp.class, "center_z", p -> p.centerZ, (p, v) -> p.centerZ = v);
	public static final PropData<StructureProp, Boolean> CULL = PropData.createBoolean(StructureProp.class, "cull", p -> p.cull, (p, v) -> p.cull = v);
	public static final PropData<StructureProp, Integer> SKY_LIGHT = PropData.createInt(StructureProp.class, "sky_light", p -> p.skyLight, (p, v) -> p.skyLight = v, 0, 15);
	public static final PropData<StructureProp, Integer> BLOCK_LIGHT = PropData.createInt(StructureProp.class, "block_light", p -> p.blockLight, (p, v) -> p.blockLight = v, 0, 15);

	@AutoRegister
	public static final PropType<StructureProp> TYPE = PropType.create("structure", StructureProp::new,
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

	public Identifier structure;
	public Supplier<StructureHolder> structureRef;
	public boolean solid;
	public boolean centerX;
	public boolean centerY;
	public boolean centerZ;
	public boolean cull;
	public int skyLight;
	public int blockLight;

	public StructureProp(PropContext<?> ctx) {
		super(ctx);
		this.structure = ID.EMPTY;
		this.structureRef = null;
		this.solid = false;
		this.centerX = true;
		this.centerY = false;
		this.centerZ = true;
		this.cull = true;
		this.skyLight = 15;
		this.blockLight = 15;
	}

	public void setStructure(Identifier id) {
		this.structure = id;
		this.structureRef = null;
	}

	@Nullable
	public StructureHolder getHolder() {
		if (structureRef == null) {
			structureRef = StructureHolder.refSupplier(structure);
		}

		return structureRef.get();
	}

	public StructureRendererData createRendererData() {
		return new StructureRendererData(centerX, centerY, centerZ, cull, skyLight, blockLight);
	}

	@Override
	public boolean isVisibleInFrustum(double x, double y, double z, FrustumCheck frustum) {
		return true;
	}
}