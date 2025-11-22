package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.util.Timestamp;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

public record SessionInfoTask(int id, UUID player, Timestamp timestamp) implements CaptureTask {
	public SessionInfoTask(DataInput in) throws IOException {
		this(in.readInt(), new UUID(in.readLong(), in.readLong()), Timestamp.read(in));
	}

	@Override
	public CaptureTaskType getTaskType() {
		return CaptureTaskType.SESSION_INFO;
	}

	@Override
	public void write(PacketCapture packetCapture, DataOutput out) throws IOException {
		out.writeInt(id);
		out.writeLong(player.getMostSignificantBits());
		out.writeLong(player.getLeastSignificantBits());
		timestamp.write(out);
	}
}
