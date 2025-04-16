package dev.latvian.mods.vidlib.feature.cutscene.event;

import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

public class CustomCutsceneEvent extends Event {
	private final Level level;
	private final WorldNumberContext context;
	private final String eventName;

	public CustomCutsceneEvent(Level level, WorldNumberContext context, String eventName) {
		this.level = level;
		this.context = context;
		this.eventName = eventName;
	}

	public Level getLevel() {
		return level;
	}

	public WorldNumberContext getContext() {
		return context;
	}

	public String getEventName() {
		return eventName;
	}
}
