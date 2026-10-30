package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.klib.io.ByteInput;
import dev.latvian.mods.klib.io.ByteOutput;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import net.minecraft.resources.Identifier;

import java.io.IOException;

public record CustomConfigPacketTask(Identifier type, byte[] data) implements CaptureTask {
	public CustomConfigPacketTask(ByteInput in) throws IOException {
		this(ID.idFromString(in.readUTF()), in.readByteArray());
	}

	@Override
	public CaptureTaskType getTaskType() {
		return CaptureTaskType.CUSTOM_CONFIG_PACKET;
	}

	@Override
	public void write(PacketCapture packetCapture, ByteOutput out) throws IOException {
		out.writeVarInt(packetCapture.getIdentifier(type));
		out.writeByteArray(data);
	}
}