package dev.latvian.mods.vidlib.feature.zone;

import net.minecraft.world.level.ClipContext;

public class ZoneClipContext extends ClipContext {
	public final Zone zone;

	public ZoneClipContext(Zone zone, ClipContext ctx) {
		super(ctx.getFrom(), ctx.getTo(), ctx.vl$getBlock(), ctx.vl$getFluid(), ctx.vl$getCollisionContext());
		this.zone = zone;
	}
}
