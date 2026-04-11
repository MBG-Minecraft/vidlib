package dev.latvian.mods.vidlib.feature.progressqueue;

import dev.latvian.mods.klib.util.StringUtils;

@FunctionalInterface
public interface ProgressItemNameFunction {
	ProgressItemNameFunction PERCENT = (progress, size) -> (progress * 100L / size) + "%";
	ProgressItemNameFunction COUNT = (progress, size) -> Long.toUnsignedString(progress) + "/" + Long.toUnsignedString(size);
	ProgressItemNameFunction BINARY_BYTE_SIZE = (progress, size) -> StringUtils.binaryByteSize(progress) + "/" + StringUtils.binaryByteSize(size);
	ProgressItemNameFunction SI_BYTE_SIZE = (progress, size) -> StringUtils.siByteSize(progress) + "/" + StringUtils.siByteSize(size);

	record OfString(String name) implements ProgressItemNameFunction {
		@Override
		public String getName(long progress, long size) {
			return name;
		}
	}

	String getName(long progress, long size);
}