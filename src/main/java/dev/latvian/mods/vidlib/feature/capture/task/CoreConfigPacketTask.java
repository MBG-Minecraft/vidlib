package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.util.IOUtils;
import net.minecraft.resources.ResourceLocation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record CoreConfigPacketTask(ResourceLocation type, byte[] data) implements CaptureTask {
	public CoreConfigPacketTask(DataInput in) throws IOException {
		this(ID.idFromString(IOUtils.readUTF(in)), IOUtils.readBytes(in));
	}

	@Override
	public CaptureTaskType getTaskType() {
		return CaptureTaskType.CORE_CONFIG_PACKET;
	}

	@Override
	public void write(PacketCapture packetCapture, DataOutput out) throws IOException {
		IOUtils.writeVarInt(out, packetCapture.getIdentifier(type));
		IOUtils.writeBytes(out, data);
	}
}