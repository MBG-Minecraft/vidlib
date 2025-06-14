package dev.latvian.mods.vidlib.util;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.LongSupplier;

public record ScheduledTask(Handler handler, RepeatingTask task, long at, MutableInt currentTick) {
	public static class Handler {
		private final LongSupplier time;
		private final List<ScheduledTask> tasks;
		private final List<ScheduledTask> newTasks;

		public Handler(LongSupplier time) {
			this.time = time;
			this.tasks = new LinkedList<>();
			this.newTasks = new ArrayList<>();
		}

		public void run(int delay, RepeatingTask task) {
			var st = new ScheduledTask(this, task, time.getAsLong() + delay, new MutableInt(0));

			if (delay > 0 || !st.tick()) {
				newTasks.add(st);
			}
		}

		public void tick() {
			if (!newTasks.isEmpty()) {
				tasks.addAll(newTasks);
				newTasks.clear();
			}

			tasks.removeIf(ScheduledTask::tick);
		}
	}

	public boolean tick() {
		if (handler.time.getAsLong() >= at) {
			int tick = currentTick.getAndIncrement();
			return !task.run(tick);
		} else {
			return false;
		}
	}
}
