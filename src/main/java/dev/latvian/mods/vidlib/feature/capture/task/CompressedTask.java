package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.klib.io.CompressionMethod;
import dev.latvian.mods.klib.io.bytes.ByteInput;
import dev.latvian.mods.klib.io.bytes.ByteOutput;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;

import java.io.IOException;

public record CompressedTask(CompressionMethod compression, CaptureTask task) implements CaptureTask {
	public static CompressedTask read(ByteInput in) throws IOException {
		var compression = CompressionMethod.of(in.readUByte());
		var bytes = in.readByteArray();
		var task = CaptureTask.readFully(ByteInput.of(compression.decompress(bytes)));
		return new CompressedTask(compression, task);
	}

	@Override
	public CaptureTaskType getTaskType() {
		return CaptureTaskType.COMPRESSED;
	}

	@Override
	public void write(PacketCapture packetCapture, ByteOutput out) throws IOException {
		out.writeUByte(compression.id);
		var bytes = ByteOutput.ofByteBuilder(8192);
		task.writeFully(packetCapture, bytes);
		out.writeByteArray(bytes.toByteArray());
	}
}