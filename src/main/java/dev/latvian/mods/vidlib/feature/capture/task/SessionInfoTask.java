package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.klib.io.bytes.ByteInput;
import dev.latvian.mods.klib.io.bytes.ByteOutput;
import dev.latvian.mods.klib.util.Timestamp;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;

import java.io.IOException;
import java.util.UUID;

public record SessionInfoTask(int id, UUID player, Timestamp timestamp) implements CaptureTask {
	public SessionInfoTask(ByteInput in) throws IOException {
		this(in.readInt(), in.readUUID(), Timestamp.read(in));
	}

	@Override
	public CaptureTaskType getTaskType() {
		return CaptureTaskType.SESSION_INFO;
	}

	@Override
	public void write(PacketCapture packetCapture, ByteOutput out) throws IOException {
		out.writeInt(id);
		out.writeUUID(player);
		timestamp.write(out);
	}
}
