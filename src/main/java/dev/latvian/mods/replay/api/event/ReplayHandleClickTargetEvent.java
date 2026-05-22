package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplaySession;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.ICancellableEvent;

public class ReplayHandleClickTargetEvent extends ReplaySessionEvent implements ICancellableEvent {
	private final HitResult hitResult;

	public ReplayHandleClickTargetEvent(ReplayAPI api, ReplaySession session, HitResult hitResult) {
		super(api, session);
		this.hitResult = hitResult;
	}

	public HitResult getHitResult() {
		return hitResult;
	}
}
