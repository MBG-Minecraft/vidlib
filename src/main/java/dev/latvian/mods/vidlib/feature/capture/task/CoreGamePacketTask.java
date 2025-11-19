package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.util.IOUtils;
import dev.latvian.mods.vidlib.util.Timestamp;
import net.minecraft.resources.ResourceLocation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record CoreGamePacketTask(Timestamp time, ResourceLocation type, byte[] data) implements CaptureTask {
	public CoreGamePacketTask(DataInput in) throws IOException {
		this(Timestamp.read(in), ID.idFromString(IOUtils.readUTF(in)), IOUtils.readBytes(in));
	}

	@Override
	public CaptureTaskType getTaskType() {
		return CaptureTaskType.CORE_GAME_PACKET;
	}

	@Override
	public void write(PacketCapture packetCapture, DataOutput out) throws IOException {
		time.write(out);
		IOUtils.writeVarInt(out, packetCapture.getIdentifier(type));
		IOUtils.writeBytes(out, data);
	}
}