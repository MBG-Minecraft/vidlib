package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplaySession;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

public class ReplayGetClickTargetEvent extends ReplaySessionEvent implements ICancellableEvent {
	private final Vec3 from;
	private final Vec3 to;
	private HitResult hitResult;

	public ReplayGetClickTargetEvent(ReplayAPI api, ReplaySession session, Vec3 from, Vec3 to, @Nullable HitResult hitResult) {
		super(api, session);
		this.from = from;
		this.to = to;
		this.hitResult = hitResult;
	}

	public Vec3 getFrom() {
		return from;
	}

	public Vec3 getTo() {
		return to;
	}

	public void setHitResult(@Nullable HitResult result) {
		if (hitResult == null) {
			hitResult = result;
		} else if (result != null && result.getLocation().distanceToSqr(from) < hitResult.getLocation().distanceToSqr(from)) {
			hitResult = result;
		}
	}

	@Nullable
	public HitResult getHitResult() {
		return hitResult;
	}
}
