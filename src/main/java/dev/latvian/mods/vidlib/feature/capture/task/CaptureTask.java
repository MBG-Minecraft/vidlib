package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.klib.io.bytes.ByteInput;
import dev.latvian.mods.klib.io.bytes.ByteOutput;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;

import java.io.IOException;
import java.util.Objects;

public interface CaptureTask {
	static CaptureTask readFully(ByteInput in) throws IOException {
		var type = Objects.requireNonNull(CaptureTaskType.LOOKUP[in.readUByte()], "Task type not found!");
		return type.factory.create(in);
	}

	CaptureTaskType getTaskType();

	void write(PacketCapture packetCapture, ByteOutput out) throws IOException;

	default void writeFully(PacketCapture packetCapture, ByteOutput out) throws IOException {
		out.writeUByte(getTaskType().id);
		write(packetCapture, out);
	}
}
