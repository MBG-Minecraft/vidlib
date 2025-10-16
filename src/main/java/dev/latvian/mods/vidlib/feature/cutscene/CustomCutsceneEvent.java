package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import net.neoforged.bus.api.Event;

public abstract class CustomCutsceneEvent extends Event {
	private final KNumberContext context;
	private final String eventName;

	protected CustomCutsceneEvent(KNumberContext context, String eventName) {
		this.context = context;
		this.eventName = eventName;
	}

	public KNumberContext getContext() {
		return context;
	}

	public String getEventName() {
		return eventName;
	}

	public static class Tick extends CustomCutsceneEvent {
		public Tick(KNumberContext context, String eventName) {
			super(context, eventName);
		}
	}

	public static class Exit extends CustomCutsceneEvent {
		public Exit(KNumberContext context, String eventName) {
			super(context, eventName);
		}
	}
}
