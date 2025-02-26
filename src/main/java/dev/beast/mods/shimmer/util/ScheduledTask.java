package dev.beast.mods.shimmer.util;

import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record ScheduledTask(Handler handler, Runnable task, long at, boolean safely) {
	public static class Handler {
		private final BlockableEventLoop<? extends Runnable> blockableEventLoop;
		private final Supplier<Level> level;
		private final List<ScheduledTask> tasks;
		private long time;

		public Handler(BlockableEventLoop<? extends Runnable> blockableEventLoop, Supplier<Level> level) {
			this.blockableEventLoop = blockableEventLoop;
			this.level = level;
			this.tasks = new ArrayList<>();
		}

		public void run(long ticks, Runnable task, boolean safely) {
			if (ticks <= 0L) {
				task.run();
			} else {
				var level = this.level.get();
				tasks.add(new ScheduledTask(this, task, level.getGameTime() + ticks, safely));
			}
		}

		public void tick() {
			time = level.get().getGameTime();
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
