package dev.latvian.mods.vidlib.util;

import dev.latvian.mods.vidlib.core.VLGameTimeProvider;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public record ScheduledTask(Handler handler, RepeatingTask task, long at, MutableInt currentTick) {
	public static class Handler {
		private final VLGameTimeProvider time;
		private final List<ScheduledTask> tasks;
		private final List<ScheduledTask> newTasks;

		public Handler(VLGameTimeProvider time) {
			this.time = time;
			this.tasks = new LinkedList<>();
			this.newTasks = new ArrayList<>();
		}

		public void run(int delay, RepeatingTask task) {
			var st = new ScheduledTask(this, task, time.vl$getGameTime() + delay, new MutableInt(0));

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
		if (handler.time.vl$getGameTime() >= at) {
			int tick = currentTick.getAndIncrement();
			return !task.run(tick);
		} else {
			return false;
		}
	}
}
