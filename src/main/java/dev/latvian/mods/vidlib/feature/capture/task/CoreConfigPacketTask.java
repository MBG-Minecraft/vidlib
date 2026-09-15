package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.klib.io.bytes.ByteInput;
import dev.latvian.mods.klib.io.bytes.ByteOutput;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public record CoreConfigPacketTask(ResourceLocation type, byte[] data) implements CaptureTask {
	public CoreConfigPacketTask(ByteInput in) throws IOException {
		this(ID.idFromString(in.readUTF()), in.readByteArray());
	}

	@Override
	public CaptureTaskType getTaskType() {
		return CaptureTaskType.CORE_CONFIG_PACKET;
	}

	@Override
	public void write(PacketCapture packetCapture, ByteOutput out) throws IOException {
		out.writeVarInt(packetCapture.getIdentifier(type));
		out.writeByteArray(data);
	}
}