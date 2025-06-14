package dev.latvian.mods.vidlib.util;

@FunctionalInterface
public interface RepeatingTask {
	record WrappedRunnable(Runnable runnable) implements RepeatingTask {
		@Override
		public boolean run(int tick) {
			runnable.run();
			return false;
		}
	}

	boolean run(int tick);
}
