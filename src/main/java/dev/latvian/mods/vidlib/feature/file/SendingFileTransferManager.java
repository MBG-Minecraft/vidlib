package dev.latvian.mods.vidlib.feature.file;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class SendingFileTransferManager {

	private static final int MAX_PACKET_SIZE = 32767;
	private record WaitingOnID(long snowflake, Consumer<Integer> consumer) {
		public void accept(int value) {
			consumer.accept(value);
		}
	}

	private static final Set<WaitingOnID> waitingOnIds = new HashSet<>();

	public static void responseToRequest(int id, long snowflake) {
		for (WaitingOnID waiting : waitingOnIds) {
			if (waiting.snowflake == snowflake) {
				waiting.accept(id);
				waitingOnIds.remove(waiting);
				break;
			}
		}
	}

	public static void sendFileInChunks(Level level, File dataFile, String destinationPath, @Nullable Consumer<Integer> onComplete) throws IOException {
		long snowflake = System.currentTimeMillis();

		List<byte[]> chunkData = new ArrayList<>();
		int totalSize = 0;
		List<byte[]> currentChunk = new ArrayList<>();
		byte[] buffer = new byte[8192];
		int bytesRead;

		try (FileInputStream fis = new FileInputStream(dataFile)) {
			while ((bytesRead = fis.read(buffer)) != -1) {
				byte[] value = new byte[bytesRead];
				System.arraycopy(buffer, 0, value, 0, bytesRead);
				if (totalSize + bytesRead > MAX_PACKET_SIZE && !currentChunk.isEmpty()) {
					byte[] chunkBuffer = new byte[totalSize];
					int offset = 0;
					for (byte[] bytes : currentChunk) {
						System.arraycopy(bytes, 0, chunkBuffer, offset, bytes.length);
						offset += bytes.length;
					}
					chunkData.add(chunkBuffer);
					currentChunk.clear();
					totalSize = 0;
				}
				currentChunk.add(value);
				totalSize += bytesRead;
			}
			if (!currentChunk.isEmpty()) {
				byte[] chunkBuffer = new byte[totalSize];
				int offset = 0;
				for (byte[] bytes : currentChunk) {
					System.arraycopy(bytes, 0, chunkBuffer, offset, bytes.length);
					offset += bytes.length;
				}
				chunkData.add(chunkBuffer);
			}
		}

		final int totalParts = chunkData.size();
		SendingFileRequestPayload requestPayload = new SendingFileRequestPayload(totalParts, snowflake, destinationPath);
		if (level.isClientSide()) {
			level.c2s(requestPayload.toC2S(level));
		} else {
			level.s2c(requestPayload.toS2C(level));
		}

		// Register callback for when a response comes in with an ID
		WaitingOnID waiting = new WaitingOnID(snowflake, id -> {
			try {
				for (int i = 0; i < totalParts; i++) {
					FileChunkPayload packet = new FileChunkPayload(id, i, chunkData.get(i));
					if (level.isClientSide()) {
						level.c2s(packet.toC2S(level));
					} else {
						level.s2c(packet.toS2C(level));
					}
				}
				if (onComplete != null) {
					onComplete.accept(id);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		waitingOnIds.add(waiting);
	}

}
