package dev.beast.mods.shimmer.feature.misc;

import net.neoforged.bus.api.Event;

public abstract class DebugTextEvent extends Event {
	public final DebugText text;

	public DebugTextEvent(DebugText text) {
		this.text = text;
	}

	public static class Render extends DebugTextEvent {
		public Render(DebugText text) {
			super(text);
		}
	}

	public static class ClientTick extends DebugTextEvent {
		public ClientTick(DebugText text) {
			super(text);
		}
	}
}
