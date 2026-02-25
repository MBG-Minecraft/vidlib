package dev.latvian.mods.vidlib.feature.progressqueue;

@FunctionalInterface
public interface ProgressItemNameFunction {
	ProgressItemNameFunction PERCENT = (progress, size) -> (progress * 100L / size) + "%";
	ProgressItemNameFunction COUNT = (progress, size) -> Long.toUnsignedString(progress) + "/" + Long.toUnsignedString(size);

	record OfString(String name) implements ProgressItemNameFunction {
		@Override
		public String getName(long progress, long size) {
			return name;
		}
	}

	String getName(long progress, long size);
}