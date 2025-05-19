package dev.latvian.mods.vidlib.util;

import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public record ScheduledTask(Handler handler, Runnable task, long at, boolean safely) {
	public static class Handler {
		private final Executor blockableEventLoop;
		private final Supplier<Level> level;
		private final List<ScheduledTask> tasks;
		private final List<ScheduledTask> newTasks;
		private long time;

		public Handler(Executor blockableEventLoop, Supplier<Level> level) {
			this.blockableEventLoop = blockableEventLoop;
			this.level = level;
			this.tasks = new ArrayList<>();
			this.newTasks = new ArrayList<>();
		}

		public void run(long ticks, Runnable task, boolean safely) {
			if (ticks <= 0L) {
				if (safely) {
					blockableEventLoop.execute(task);
				} else {
					task.run();
				}
			} else {
				var level = this.level.get();
				newTasks.add(new ScheduledTask(this, task, level.getGameTime() + ticks, safely));
			}
		}

		public void tick() {
			time = level.get().getGameTime();

			if (!newTasks.isEmpty()) {
				tasks.addAll(newTasks);
				newTasks.clear();
			}

			tasks.removeIf(ScheduledTask::tick);
		}
	}

	public boolean tick() {
		if (handler.time >= at) {
			if (safely) {
				handler.blockableEventLoop.execute(task);
			} else {
				task.run();
			}

			return true;
		} else {
			return false;
		}
	}
}
