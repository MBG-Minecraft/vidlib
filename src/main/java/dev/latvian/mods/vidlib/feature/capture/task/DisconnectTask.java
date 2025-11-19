package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.util.Timestamp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record DisconnectTask(Timestamp timestamp) implements CaptureTask {
	public DisconnectTask(DataInput in) throws IOException {
		this(Timestamp.read(in));
	}

	@Override
	public CaptureTaskType getTaskType() {
		return CaptureTaskType.DISCONNECT;
	}

	@Override
	public void write(PacketCapture packetCapture, DataOutput out) throws IOException {
		timestamp.write(out);
	}
}