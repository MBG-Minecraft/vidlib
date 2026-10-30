package dev.latvian.mods.vidlib.feature.zone;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

public class ZoneClipContext {
	public final Zone zone;
	public final Vec3 from;
	public final Vec3 to;

	public ZoneClipContext(Zone zone, ClipContext ctx) {
		this(zone, ctx.getFrom(), ctx.getTo());
	}

	public ZoneClipContext(Zone zone, Vec3 from, Vec3 to) {
		this.zone = zone;
		this.from = from;
		this.to = to;
	}

	public Vec3 getFrom() {
		return from;
	}

	public Vec3 getTo() {
		return to;
	}
}
