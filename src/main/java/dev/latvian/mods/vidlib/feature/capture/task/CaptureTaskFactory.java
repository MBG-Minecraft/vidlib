package dev.latvian.mods.vidlib.feature.capture.task;

import java.io.DataInput;
import java.io.IOException;

@FunctionalInterface
public interface CaptureTaskFactory {
	CaptureTask create(DataInput in) throws IOException;
}