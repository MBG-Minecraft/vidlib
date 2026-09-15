package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.klib.io.bytes.ByteInput;
import dev.latvian.mods.klib.io.bytes.ByteOutput;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.Timestamp;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public record CustomGamePacketTask(Timestamp time, ResourceLocation type, byte[] data) implements CaptureTask {
	public CustomGamePacketTask(ByteInput in) throws IOException {
		this(Timestamp.read(in), ID.idFromString(in.readUTF()), in.readByteArray());
	}

	@Override
	public CaptureTaskType getTaskType() {
		return CaptureTaskType.CUSTOM_GAME_PACKET;
	}

	@Override
	public void write(PacketCapture packetCapture, ByteOutput out) throws IOException {
		time.write(out);
		out.writeVarInt(packetCapture.getIdentifier(type));
		out.writeByteArray(data);
	}
}