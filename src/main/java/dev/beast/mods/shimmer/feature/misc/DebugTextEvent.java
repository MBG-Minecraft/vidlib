package dev.beast.mods.shimmer.feature.misc;

import net.neoforged.bus.api.Event;

public abstract class DebugTextEvent extends Event {
	public final ScreenText text;

	public DebugTextEvent(ScreenText text) {
		this.text = text;
	}

	public static class Render extends DebugTextEvent {
		public Render(ScreenText text) {
			super(text);
		}
	}

	public static class ClientTick extends DebugTextEvent {
		public ClientTick(ScreenText text) {
			super(text);
		}
	}
}
