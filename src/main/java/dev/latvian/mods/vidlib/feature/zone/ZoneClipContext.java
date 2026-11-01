package dev.latvian.mods.vidlib.feature.zone;

import net.minecraft.world.level.ClipContext;

public class ZoneClipContext extends ClipContext {
	public final ResolvedZone zone;

	public ZoneClipContext(ResolvedZone zone, ClipContext ctx) {
		super(ctx.getFrom(), ctx.getTo(), ctx.block, ctx.fluid, ctx.collisionContext);
		this.zone = zone;
	}
}