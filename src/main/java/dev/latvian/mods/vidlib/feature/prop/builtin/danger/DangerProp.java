package dev.latvian.mods.vidlib.feature.prop.builtin.danger;

import dev.latvian.mods.klib.block.BlockUtils;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;

public class DangerProp extends Prop {
	public static final PropData<DangerProp, Float> WIDTH_MOD = PropData.createFloat(DangerProp.class, "width_mod", p -> p.widthMod, (p, v) -> p.widthMod = v, 0F, 1F);
	public static final PropData<DangerProp, Float> MAX_HEIGHT = PropData.createFloat(DangerProp.class, "max_height", p -> p.maxHeight, (p, v) -> p.maxHeight = v, 0F, 200F);
	public static final PropData<DangerProp, Boolean> BLINK = PropData.createBoolean(DangerProp.class, "blink", p -> p.blink, (p, v) -> p.blink = v);

	@AutoRegister
	public static final PropType<DangerProp> TYPE = PropType.create("danger", DangerProp::new,
		POSITION,
		WIDTH,
		WIDTH_MOD,
		MAX_HEIGHT,
		BLINK
	);

	public double groundY = Double.NaN;
	public float widthMod = 0.2F;
	public float maxHeight = 50F;
	public boolean blink = true;

	public DangerProp(PropContext<?> ctx) {
		super(ctx);
		this.width = 12F;
	}

	@Override
	public void tick() {
		super.tick();
		groundY = BlockUtils.getGroundY(level, pos.x, pos.y, pos.z);
	}

	@Override
	public boolean isVisibleInFrustum(double x, double y, double z, FrustumCheck frustum) {
		return true;
	}
}
