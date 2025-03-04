package dev.beast.mods.shimmer.feature.clock;

import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

public class ClockEvent extends Event {
	private final Level level;
	private final Clock clock;
	private final int tick;
	private final String eventName;
	private final boolean finished;

	public ClockEvent(Level level, Clock clock, int tick, String eventName) {
		this.level = level;
		this.clock = clock;
		this.tick = tick;
		this.eventName = eventName;
		this.finished = eventName.equals("finished");
	}

	public Level getLevel() {
		return level;
	}

	public Clock getClock() {
		return clock;
	}

	public int getTick() {
		return tick;
	}

	public int getTicksRemaining() {
		return clock.maxTicks() - tick;
	}

	public String getEventName() {
		return eventName;
	}

	public boolean isFinished() {
		return finished;
	}
}
