package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplaySession;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import net.minecraft.world.entity.Entity;

public class ReplayEntityMenuEvent extends ReplayImGuiEventEvent {
	private final Entity entity;

	public ReplayEntityMenuEvent(ReplayAPI api, ReplaySession session, ImGraphics graphics, Entity entity) {
		super(api, session, graphics);
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}
}
