package dev.mrbeastgaming.mods.hub.file;

import com.github.luben.zstd.Zstd;
import dev.latvian.mods.klib.io.CompressionMethod;
import dev.latvian.mods.klib.io.CountingOutputStream;
import dev.latvian.mods.klib.io.IOConsumer;
import dev.latvian.mods.klib.io.bytes.ByteOutput;
import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItem;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItemNameFunction;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressingInputStream;
import dev.mrbeastgaming.mods.hub.api.Auth;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubProjectFileLinkRequest;
import dev.mrbeastgaming.mods.hub.api.HubUploadRequest;
import dev.mrbeastgaming.mods.hub.api.data.HubGatewayInfo;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import dev.mrbeastgaming.mods.hub.api.data.HubProjectFileLink;
import dev.mrbeastgaming.mods.hub.api.gateway.HubCommonGateway;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

public record HubUploadContext(
	String projectToken,
	HubProject project,
	@Nullable ProgressQueue checkQueue,
	@Nullable ProgressQueue uploadQueue,
	@Nullable UUID minecraftId
) {
	private record HubFileUploadsImpl(HubUploadContext ctx, Path gameDir, List<HubUploadRequestItem> requestItems) implements HubFileUploads {
		@Override
		public HubUploadContext getUploadContext() {
			return ctx;
		}

		@Override
		public Path getGameDirectory() {
			return gameDir;
		}

		@Override
		public void add(HubFileInfo info, HubUploadBuilder builder) {
			try {
				builder.build(info, requestItems::add);
			} catch (IOException ex) {
				VidLib.LOGGER.error("Failed to build HubUploadRequestItem from " + info.fullPath(), ex);
			}
		}
	}

	@Nullable
	public ProgressItem createCheckItem(String fileName) {
		if (checkQueue == null) {
			return null;
		}

		var item = checkQueue.addItem(fileName);
		item.setInfoText(ProgressItemNameFunction.BINARY_BYTE_SIZE);
		item.setBlocksExit(true);
		return item;
	}

	@Nullable
	public ProgressItem createUploadItem(String fileName) {
		if (uploadQueue == null) {
			return null;
		}

		var item = uploadQueue.addItem(fileName);
		item.setInfoText(ProgressItemNameFunction.BINARY_BYTE_SIZE);
		item.setBlocksExit(true);
		return item;
	}

	@ApiStatus.Internal
	public List<HubUploadResponseItem> uploadBlocking(
		@Nullable HubProject project,
		@Nullable HubGatewayInfo gatewayInfo,
		@Nullable HubCommonGateway<?> gateway,
		int parallelThreads,
		String intent,
		IOConsumer<HubFileUploads> callback
	) {
		if (gatewayInfo == null) {
			return List.of();
		}

		var requestItems = new ArrayList<HubUploadRequestItem>();

		try {
			callback.accept(new HubFileUploadsImpl(this, PlatformHelper.CURRENT.getGameDirectory().toAbsolutePath().toRealPath(), requestItems));
		} catch (IOException ex) {
			VidLib.LOGGER.error("Failed to collect files to upload", ex);
		}

		if (requestItems.isEmpty()) {
			return List.of();
		}

		var response = HubAPI.CoreAPI.postUpload(new HubUploadRequest(
			intent,
			gatewayInfo.token(),
			requestItems.stream().map(HubUploadRequestItem::file).toList()
		));

		var map = requestItems.stream().collect(Collectors.toMap(v -> v.file().id(), Function.identity()));
		var responseItems = new ArrayList<HubUploadResponseItem>(requestItems.size());

		try (var gatewayExecutor = Executors.newSingleThreadExecutor();
			 var httpExecutor = parallelThreads == 1 ? gatewayExecutor : Executors.newFixedThreadPool(parallelThreads)) {
			var list = new ArrayList<CompletableFuture<?>>();

			for (var entry : response.files()) {
				var requestItem = map.get(entry.id());

				if (requestItem == null) {
					throw new NullPointerException("Path of " + entry.id() + " not found");
				}

				var file = requestItem.info().file();

				if (Files.notExists(file)) {
					throw new NullPointerException("File " + file.toString() + " not found");
				}

				if (entry.offset() == entry.size()) {
					responseItems.add(new HubUploadResponseItem(HubUploadResponseItem.Status.SKIPPED, requestItem));
					continue;
				}

				var fileName = file.getFileName().toString();

				long offset = entry.offset();

				if (offset > 0L) {
					var progressItem = createCheckItem(entry.id());

					if (progressItem != null) {
						progressItem.setSize(entry.offset());
						progressItem.display();
					}

					try {
						var partialChecksum = entry.offsetChecksum().type().digest(file, 0L, entry.offset(), progressItem);

						if (!partialChecksum.equals(entry.offsetChecksum())) {
							VidLib.LOGGER.info("Partial checksum of " + fileName + " didn't match, restarting upload");
							offset = 0L;
						}
					} catch (Exception ex) {
						throw new RuntimeException("Error generating checksum of " + fileName, ex);
					} finally {
						if (progressItem != null) {
							progressItem.remove();
						}
					}
				}

				var httpUpload = httpUpload(
					entry.token(),
					fileName,
					file,
					offset,
					response.httpBodyLimit(),
					httpExecutor
				);

				if (httpUpload != null) {
					list.add(httpUpload);
					responseItems.add(new HubUploadResponseItem(HubUploadResponseItem.Status.HTTP, requestItem));
					continue;
				}

				var offset1 = offset;
				var gatewayFuture = gateway == null ? CompletableFuture.failedFuture(new NullPointerException("Gateway can't be null")) : CompletableFuture.runAsync(() -> {
					try {
						gatewayUpload(
							gateway,
							entry.token(),
							fileName,
							file,
							offset1
						);
					} catch (Exception ex) {
						VidLib.LOGGER.error("Failed to get path of file " + entry.id(), ex);
					}
				}, gatewayExecutor);

				list.add(gatewayFuture);

				responseItems.add(new HubUploadResponseItem(HubUploadResponseItem.Status.GATEWAY, requestItem));
			}

			CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
		}

		var links = new ArrayList<HubProjectFileLink>();

		for (var item : requestItems) {
			if (item.link() != null) {
				links.add(new HubProjectFileLink(item.file().checksum(), item.link()));
			}
		}

		if (project != null && !links.isEmpty()) {
			try {
				var uploads = HubAPI.ProjectAPI.postLinkFiles(project.id(), new HubProjectFileLinkRequest(links)).uploads();

				for (var upload : uploads.entrySet()) {
					VidLib.LOGGER.info("Upload " + upload.getKey() + ": " + upload.getValue());
				}
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to link files", ex);
			}
		}

		int skipped = 0;

		for (var item : responseItems) {
			if (item.status() == HubUploadResponseItem.Status.SKIPPED) {
				skipped++;
			}
		}

		VidLib.LOGGER.info("Finished uploading %,d '%s' files (%,d skipped)".formatted(responseItems.size(), intent, skipped));
		return responseItems;
	}

	@Nullable
	private CompletableFuture<Void> httpUpload(String token, String fileName, Path path, long offset, long limit, Executor executor) {
		if (limit <= 0L) {
			return null;
		}

		var compressionItem = createCheckItem(fileName);
		var compressedSuccessfully = false;

		try {
			var compressedSize = new CountingOutputStream();

			if (compressionItem != null) {
				compressionItem.setInfoText("Compressing...");
				compressionItem.setSize(1L);
				compressionItem.addProgress(1L);
				compressionItem.display();
			}

			try (var out = CompressionMethod.ZSTD.out(compressedSize); var in = Files.newInputStream(path)) {
				in.skipNBytes(offset);
				in.transferTo(out);
			} catch (Exception ex) {
				return null;
			}

			if (compressedSize.getCount() > Math.min(limit, 104857600L)) {
				return null;
			}

			compressedSuccessfully = true;
		} finally {
			if (compressionItem != null && !compressedSuccessfully) {
				compressionItem.remove();
			}
		}

		return CompletableFuture.runAsync(() -> {
			var uploadItem = createUploadItem(fileName);

			try {
				byte[] compressed;

				try (var in = Files.newInputStream(path)) {
					in.skipNBytes(offset);
					compressed = CompressionMethod.ZSTD.compress(in.readAllBytes());
				} finally {
					if (compressionItem != null) {
						compressionItem.remove();
					}
				}

				if (uploadItem != null) {
					uploadItem.setInfoText(ProgressItemNameFunction.BINARY_BYTE_SIZE);
					uploadItem.setSize(compressed.length);
					uploadItem.display();
				}

				var response = HubAPI.send(HubAPI.request("api/file-storage", Auth.NOT_REQUIRED)
					.setHeader("X-MBG-Hub-File-Storage-Token", token)
					.setHeader("X-MBG-Hub-Compression-Method", CompressionMethod.ZSTD.name)
					.setHeader("X-MBG-Hub-Offset", Long.toUnsignedString(offset))
					.setHeader("X-Content-Length-Hint", Long.toUnsignedString(compressed.length))
					.POST(ProgressingInputStream.wrapBodyPublisher(compressed, 0, compressed.length, uploadItem))
					.build(), false
				);

				if (response.code / 100 != 2) {
					throw new IOException("HTTP Error " + response.code + " uploading " + fileName);
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			} finally {
				if (uploadItem != null) {
					uploadItem.remove();
				}
			}
		}, executor);
	}

	private void gatewayUpload(
		HubCommonGateway<?> gateway,
		String token,
		String fileName,
		Path file,
		long offset
	) throws IOException {
		long size = Files.size(file);

		int maxChunkSize = (int) Math.min(size, 4_194_304); // 4 MiB chunk
		long remaining = size - offset;
		int totalChunks = Mth.ceil(remaining / (float) maxChunkSize);

		var progressItem = createUploadItem(fileName);

		if (progressItem != null) {
			progressItem.setSize(remaining);
			progressItem.setInfoText(ProgressItemNameFunction.BINARY_BYTE_SIZE);
			progressItem.display();
		}

		VidLib.LOGGER.info("Uploading %,d bytes of %s".formatted(remaining, fileName));

		int chunkIndex = 0;

		var decompressedBuffer = ByteBuffer.allocateDirect(maxChunkSize);
		var compressedBuffer = ByteBuffer.allocateDirect((int) Zstd.compressBound(maxChunkSize));

		try (var channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
			channel.position(offset);

			while (remaining > 0L) {
				decompressedBuffer.clear().limit((int) Math.min(maxChunkSize, remaining));

				while (decompressedBuffer.hasRemaining()) {
					if (channel.read(decompressedBuffer) == -1 && decompressedBuffer.hasRemaining()) {
						throw new EOFException();
					}
				}

				decompressedBuffer.flip();

				int decompressedSize = decompressedBuffer.remaining();

				if (decompressedSize == 0) {
					break;
				}

				compressedBuffer.clear();

				var result = Zstd.compressDirectByteBuffer(compressedBuffer, 0, compressedBuffer.remaining(), decompressedBuffer, 0, decompressedSize, Zstd.defaultCompressionLevel());

				if (Zstd.isError(result)) {
					throw new IOException("Error compressing buffer: " + Zstd.getErrorName(result));
				}

				compressedBuffer.rewind().limit((int) result);

				var data = ByteOutput.ofByteBuilder(16);
				data.writeVarInt(HubCommonGateway.PACKET_UPLOAD_CHUNK);
				data.writeVarInt(chunkIndex);
				data.writeVarInt(totalChunks);
				data.writeUTF(token);
				data.writeVarLong(offset);
				data.writeVarInt(decompressedSize);
				data.writeVarInt(CompressionMethod.ZSTD.id);
				data.writeVarInt(compressedBuffer.remaining());
				var metadata = ByteBuffer.wrap(data.toByteArray());

				var finalBuffer = ByteBuffer.allocateDirect(metadata.remaining() + compressedBuffer.remaining());
				finalBuffer.put(metadata);
				finalBuffer.put(compressedBuffer);
				finalBuffer.flip();
				gateway.send(finalBuffer).join();

				offset += decompressedSize;
				remaining -= decompressedSize;
				chunkIndex++;

				if (progressItem != null) {
					progressItem.addProgress(decompressedSize);

					if (progressItem.queue.isCancelled()) {
						return;
					}
				}
			}

			var data = ByteOutput.ofByteBuilder(3 + token.length());
			data.writeVarInt(HubCommonGateway.PACKET_UPLOAD_END);
			data.writeUTF(token);
			gateway.send(data).join();
		} finally {
			if (progressItem != null) {
				progressItem.remove();
			}
		}
	}
}
