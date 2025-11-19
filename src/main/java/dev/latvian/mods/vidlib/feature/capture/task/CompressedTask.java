package dev.latvian.mods.vidlib.feature.capture.task;

import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public record CompressedTask(CaptureTask task) implements CaptureTask {
	private static CaptureTask readCompressed(DataInput in) throws IOException {
		var bytes = IOUtils.readBytes(in);

		try (var cin = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)))) {
			return CaptureTask.readFully(cin);
		}
	}

	public CompressedTask(DataInput in) throws IOException {
		this(readCompressed(in));
	}

	@Override
	public CaptureTaskType getTaskType() {
		return CaptureTaskType.COMPRESSED;
	}

	@Override
	public void write(PacketCapture packetCapture, DataOutput out) throws IOException {
		var bytes = new ByteArrayOutputStream();

		try (var cout = new DataOutputStream(new GZIPOutputStream(bytes))) {
			task.writeFully(packetCapture, cout);
		}

		IOUtils.writeBytes(out, bytes.toByteArray());
	}
}