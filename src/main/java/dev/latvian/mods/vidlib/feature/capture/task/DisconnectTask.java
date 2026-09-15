package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.klib.io.bytes.ByteInput;
import dev.latvian.mods.klib.io.bytes.ByteOutput;
import dev.latvian.mods.klib.util.Timestamp;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;

import java.io.IOException;

public record DisconnectTask(Timestamp timestamp) implements CaptureTask {
	public DisconnectTask(ByteInput in) throws IOException {
		this(Timestamp.read(in));
	}

	@Override
	public CaptureTaskType getTaskType() {
		return CaptureTaskType.DISCONNECT;
	}

	@Override
	public void write(PacketCapture packetCapture, ByteOutput out) throws IOException {
		timestamp.write(out);
	}
}