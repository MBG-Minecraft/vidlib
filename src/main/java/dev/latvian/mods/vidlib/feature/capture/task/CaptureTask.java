package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.vidlib.feature.capture.PacketCapture;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public interface CaptureTask {
	static CaptureTask readFully(DataInput in) throws IOException {
		var type = Objects.requireNonNull(CaptureTaskType.LOOKUP[in.readByte() & 0xFF], "Task type not found!");
		return type.factory.create(in);
	}

	CaptureTaskType getTaskType();

	void write(PacketCapture packetCapture, DataOutput out) throws IOException;

	default void writeFully(PacketCapture packetCapture, DataOutput out) throws IOException {
		out.writeByte(getTaskType().id);
		write(packetCapture, out);
	}
}
