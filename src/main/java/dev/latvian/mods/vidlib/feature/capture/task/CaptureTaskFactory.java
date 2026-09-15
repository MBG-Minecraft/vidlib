package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.klib.io.bytes.ByteInput;

import java.io.IOException;

@FunctionalInterface
public interface CaptureTaskFactory {
	CaptureTask create(ByteInput in) throws IOException;
}