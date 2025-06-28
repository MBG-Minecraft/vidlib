package dev.latvian.mods.vidlib.feature.cutscene.event;

import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

public class CustomCutsceneEvent extends Event {
	private final Level level;
	private final KNumberContext context;
	private final String eventName;

	public CustomCutsceneEvent(Level level, KNumberContext context, String eventName) {
		this.level = level;
		this.context = context;
		this.eventName = eventName;
	}

	public Level getLevel() {
		return level;
	}

	public KNumberContext getContext() {
		return context;
	}

	public String getEventName() {
		return eventName;
	}
}
