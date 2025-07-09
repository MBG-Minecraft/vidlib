package dev.latvian.mods.vidlib.feature.file;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.latvian.mods.vidlib.VidLib;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;

public class ReceivingFileTransferManager {

	private static final Cache<Integer, FileChunkAssembler> chunkCache =
		CacheBuilder.newBuilder()
			.expireAfterWrite(10, java.util.concurrent.TimeUnit.MINUTES)
			.build();

	private static class FileChunkAssembler {
		private final byte[][] chunks;
		private final int totalParts;
		private final String path;
		private int receivedParts;

		public FileChunkAssembler(int totalParts, String path) {
			this.totalParts = totalParts;
			this.path = path;
			this.chunks = new byte[totalParts][];
			this.receivedParts = 0;
		}

		public synchronized void addChunk(int partIndex, byte[] chunk) {
			if (partIndex >= 0 && partIndex < totalParts && chunks[partIndex] == null) {
				chunks[partIndex] = chunk;
				receivedParts++;
			}
		}

		public boolean isComplete() {
			return receivedParts == totalParts;
		}

		public byte[] assemble() {
			if (!isComplete()) {
				throw new IllegalStateException("File assembly called before all chunks were received.");
			}

			int totalSize = 0;
			for (byte[] chunk : chunks) {
				totalSize += chunk.length;
			}

			byte[] assembled = new byte[totalSize];
			int offset = 0;
			for (byte[] chunk : chunks) {
				System.arraycopy(chunk, 0, assembled, offset, chunk.length);
				offset += chunk.length;
			}
			return assembled;
		}
	}

	/**
	 * Generates a random unique ID for file transfers.
	 * It's random for security reasons being able to read the packet data.
	 * You'd be able to expect the next ID if it was sequential.
	 */
	public static int getRandomId(int totalParts, String path) {
		int id;
		do {
			id = (int) (Math.random() * Integer.MAX_VALUE);
		} while (chunkCache.getIfPresent(id) != null);
		chunkCache.put(id, new FileChunkAssembler(totalParts, path));
		return id;
	}

	public static void addChunk(int id, int partIndex, byte[] chunk) {
		FileChunkAssembler assembler = chunkCache.getIfPresent(id);
		if (assembler != null) {
			assembler.addChunk(partIndex, chunk);
			if (assembler.isComplete()) {
				byte[] assembledData = assembler.assemble();
				chunkCache.invalidate(id);
				Path gameDir = FMLPaths.GAMEDIR.get();
				Path filePath = gameDir.resolve(assembler.path);
				try {
					Files.write(filePath, assembledData);
					VidLib.LOGGER.info("File transfer complete: {}", filePath);
				} catch (java.io.IOException e) {
					VidLib.LOGGER.error("Failed to write file: {}", e.getMessage());
				}
			}
		} else {
			throw new IllegalArgumentException("No file transfer found with ID: " + id);
		}
	}

}
