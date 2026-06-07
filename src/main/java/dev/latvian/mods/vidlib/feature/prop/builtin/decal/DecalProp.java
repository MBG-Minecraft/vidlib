package dev.latvian.mods.vidlib.feature.prop.builtin.decal;

import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.decal.Decal;
import dev.latvian.mods.vidlib.feature.decal.DecalImBuilder;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class DecalProp extends Prop {
	@AutoRegister
	public static final PropType<DecalProp> TYPE = PropType.create(VidLib.id("decal"), DecalProp::new,
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
		PropData.create(DecalProp.class, "decal", Decal.DATA_TYPE, p -> p.decal, (p, v) -> p.decal = v, DecalImBuilder.TYPE)
	);

	public Decal decal;

	public DecalProp(PropContext<?> ctx) {
		super(ctx);
		this.decal = new Decal(new Vector3d());
		this.width = 1F;
		this.height = 1F;
		this.canCollide = false;
		this.canInteract = false;
	}

	@Override
	public boolean isVisible(double x, double y, double z, FrustumCheck frustum, Vec3 camera, double squaredCenterDistanceToCamera) {
		return true;
	}
}
