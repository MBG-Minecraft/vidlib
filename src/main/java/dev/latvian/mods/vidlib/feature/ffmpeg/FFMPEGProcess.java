package dev.latvian.mods.vidlib.feature.ffmpeg;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

public class FFMPEGProcess implements AutoCloseable {
	private Process process;
	public final WritableByteChannel outputChannel;
	public final OutputStream outputStream;
	public final ReadableByteChannel inputChannel;
	public final InputStream inputStream;
	private boolean closeForcibly;

	public static List<String> text(List<String> command) throws IOException, InterruptedException {
		var list = new ArrayList<String>();

		try (var process = new FFMPEGProcess(command).waitFor();
		     var reader = new BufferedReader(new InputStreamReader(process.inputStream))
		) {
			String line;

			while ((line = reader.readLine()) != null) {
				list.add(line);
			}
		}

		return list;
	}

	public static FFMPEGProcess stream(int width, int height) throws IOException {
		return stream(width, height, List.of(), List.of());
	}

	public static FFMPEGProcess stream(int width, int height, List<String> inputArgs, List<String> outputArgs) throws IOException {
		var commands = new ArrayList<String>(24);
		commands.add(FFMPEGBinaries.INSTANCE.ffmpeg());

		commands.add("-f");
		commands.add("rawvideo");

		commands.add("-pix_fmt");
		commands.add("rgba");

		commands.add("-s");
		commands.add(width + "x" + height);

		commands.add("-noautorotate");

		commands.addAll(inputArgs);

		commands.add("-i");
		commands.add("pipe:0");

		commands.add("-an");

		commands.addAll(outputArgs);

		commands.add("-vf");
		commands.add("vflip");

		commands.add("-c:v");
		commands.add("libx264");

		commands.add("-pix_fmt");
		commands.add("yuv420p");

		commands.add("-movflags");
		commands.add("frag_keyframe+empty_moov");

		commands.add("-f");
		commands.add("mp4");

		commands.add("-shortest"); // idk

		commands.add("pipe:1");
		return new FFMPEGProcess(commands);
	}

	public FFMPEGProcess(List<String> command) throws IOException {
		this.closeForcibly = false;

		this.process = new ProcessBuilder(command)
			.redirectError(ProcessBuilder.Redirect.to(FFMPEGBinaries.FFMPEG_LOG_FILE.get()))
			.directory(PlatformHelper.CURRENT.getGameDirectory().toFile())
			.start();

		this.outputStream = process.getOutputStream();
		this.outputChannel = Channels.newChannel(this.outputStream);

		this.inputStream = process.getInputStream();
		this.inputChannel = Channels.newChannel(this.inputStream);
	}

	public FFMPEGProcess waitFor() throws InterruptedException {
		process.waitFor();
		return this;
	}

	public void setCloseForcibly(boolean closeForcibly) {
		this.closeForcibly = closeForcibly;
	}

	public boolean isOpen() {
		return isAlive() && this.inputChannel != null && this.inputChannel.isOpen();
	}

	public boolean isAlive() {
		return this.process != null && this.process.isAlive();
	}

	public @Nullable Process getProcess() {
		return this.process;
	}

	public void write(ByteBuffer byteBuffer) throws IOException {
		if (!this.isOpen() || !outputChannel.isOpen()) {
			VidLib.LOGGER.warn("FFMPEG process not open. Failed to write.");
			return;
		}
		this.outputChannel.write(byteBuffer);
	}

	public void read(ByteBuffer byteBuffer) throws IOException {
		if (!this.isOpen() || !inputChannel.isOpen()) {
			VidLib.LOGGER.warn("FFMPEG process not open. Failed to read.");
			return;
		}

		int bytesRead = 0;
		while (bytesRead < byteBuffer.capacity()) {
			int result = this.inputChannel.read(byteBuffer);
			if (result == -1) {
				break;
			}
			bytesRead += result;
		}
	}

	@Override
	public void close() throws IOException {
		this.outputChannel.close();
		this.inputChannel.close();

		this.outputStream.flush();
		this.outputStream.close();
		this.inputChannel.close();

		this.process.getErrorStream().close();

		this.process.destroyForcibly();
		this.process = null;

		if (closeForcibly) {
			try {
				if (SystemUtils.IS_OS_WINDOWS) {
					Runtime.getRuntime().exec(new String[]{"taskkill", "/IM", "ffmpeg.exe", "/F"});
				} else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
					Runtime.getRuntime().exec(new String[]{"pkill", "-f", "ffmpeg"});
				}
			} catch (IOException e) {
				VidLib.LOGGER.error("Failed to kill FFMPEG process: " + e.getMessage());
			}
		}
	}
}
