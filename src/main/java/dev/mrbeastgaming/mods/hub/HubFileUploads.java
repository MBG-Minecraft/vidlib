package dev.mrbeastgaming.mods.hub;

import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.util.MD5;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItem;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItemNameFunction;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressingInputStream;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.project.ProjectUploadRequestItem;
import dev.mrbeastgaming.mods.hub.api.project.ProjectUploadResponseItem;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class HubFileUploads {
	public record SyncedFile(Path path, String name, HubFileSyncMetadata meta, Mutable<ProgressItem> progressItem, ProjectUploadRequestItem item) {
	}

	public static List<SyncedFile> syncDirectory(
		Path directory,
		Predicate<Path> filter,
		@Nullable FileTypeProvider contentType,
		@Nullable UniqueIdProvider uniqueIdProvider,
		@Nullable UUID minecraftId,
		@Nullable ProgressQueue progressQueue
	) {
		try (var stream = Files.walk(directory)) {
			var fileList = stream
				.filter(Files::isRegularFile)
				.filter(Files::isReadable)
				.filter(path -> !path.getFileName().toString().endsWith(".mbg-hub-synced"))
				.filter(filter)
				.toList();

			return syncFiles(fileList, contentType, uniqueIdProvider, minecraftId, progressQueue);
		} catch (Exception ex) {
			return List.of();
		}
	}

	public static List<SyncedFile> syncFile(
		Path file,
		@Nullable FileTypeProvider fileTypeProvider,
		@Nullable UniqueIdProvider uniqueIdProvider,
		@Nullable UUID minecraftId,
		@Nullable ProgressQueue progressQueue
	) {
		return syncFiles(List.of(file), fileTypeProvider, uniqueIdProvider, minecraftId, progressQueue);
	}

	private static List<SyncedFile> syncFiles(
		List<Path> fileList,
		@Nullable FileTypeProvider fileTypeProvider,
		@Nullable UniqueIdProvider uniqueIdProvider,
		@Nullable UUID minecraftId,
		@Nullable ProgressQueue progressQueue
	) {
		var projectConfig = HubProjectConfig.INSTANCE.get();

		if (projectConfig == null) {
			return List.of();
		}

		var resultFiles = new ArrayList<SyncedFile>(fileList.size());
		var progressItems = new ArrayList<ProgressItem>(fileList.size());
		var map = new LinkedHashMap<MD5, SyncedFile>();

		try {
			if (progressQueue != null) {
				progressQueue.topText = "Checking files...";

				for (var file : fileList) {
					var progressItem = progressQueue.addItem(file.getFileName().toString(), ProgressItemNameFunction.SI_BYTE_SIZE);
					progressItem.setSize(Files.size(file));
					progressItems.add(progressItem);
				}

				progressQueue.display();
			}

			for (int i = 0; i < fileList.size(); i++) {
				var file = fileList.get(i);
				var name = file.getFileName().toString();
				var progressItem = progressQueue == null ? null : progressItems.get(i);

				try {
					if (progressItem != null) {
						progressItem.setStarted();
					}

					var meta = HubFileSyncMetadata.load(file, progressItem);

					if (meta.changed()) {
						HubFileSyncMetadata.save(file, meta);
						Files.setLastModifiedTime(file, FileTime.from(meta.lastModified()));
						VidLib.LOGGER.info("Updated metadata of " + name + ": " + meta);
					}

					var fileType = fileTypeProvider == null ? FileTypeProvider.probe(file) : fileTypeProvider.getFileType(file);

					var syncFile = new SyncedFile(file, name, meta, new MutableObject<>(), new ProjectUploadRequestItem(
						uniqueIdProvider == null ? MD5.NIL : uniqueIdProvider.getUniqueId(file),
						meta.checksum(),
						meta.size(),
						name,
						fileType,
						minecraftId
					));

					map.put(syncFile.item.checksum(), syncFile);
				} catch (Exception ex) {
					VidLib.LOGGER.error("Failed to sync Beast Hub file " + name, ex);
				} finally {
					if (progressItem != null) {
						progressItem.setDone();
					}
				}
			}

			if (progressQueue != null) {
				progressQueue.clear();
				progressItems.clear();
				progressQueue.topText = "Uploading files...";
			}

			if (!map.isEmpty()) {
				var list = HubAPI.apiProjectUpload(projectConfig.token().toString(), map.values().stream().map(SyncedFile::item).toList());

				if (progressQueue != null) {
					for (var item : list) {
						var syncFile = map.get(item.checksum());

						if (syncFile != null) {
							var progressItem = progressQueue.addItem(syncFile.name, ProgressItemNameFunction.SI_BYTE_SIZE);
							progressItem.setSize(syncFile.meta.size());
							syncFile.progressItem.setValue(progressItem);
						}
					}

					progressQueue.display();
				}

				byte[] chunk = null;

				for (var item : list) {
					var syncFile = map.get(item.checksum());

					if (syncFile != null) {
						var progressItem = syncFile.progressItem.getValue();

						if (progressItem != null) {
							progressItem.setStarted();
						}

						try {
							if (chunk == null) {
								long maxSize = 0L;

								for (var item1 : map.values()) {
									maxSize = item1.meta.size();
								}

								chunk = new byte[(int) Math.min(maxSize, item.maxChunkSize())];
							}

							resultFiles.add(syncFile1(syncFile, item, chunk, progressItem));
						} catch (Exception ex) {
							if (progressItem != null) {
								progressItem.error(ex.getMessage());
							}

							VidLib.LOGGER.error("Failed to sync Beast Hub file " + syncFile.name, ex);
						} finally {
							if (progressItem != null) {
								progressItem.setDone();
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to sync Beast Hub files", ex);
		} finally {
			if (progressQueue != null) {
				progressQueue.topText = "Files Uploaded";
				progressQueue.bottomText = "";

				for (var syncFile : map.values()) {
					var progressItem = syncFile.progressItem.getValue();

					if (progressItem != null) {
						progressItem.setDone();
					}
				}
			}
		}

		return resultFiles;
	}

	private static SyncedFile syncFile1(SyncedFile file, ProjectUploadResponseItem item, byte[] chunk, @Nullable ProgressItem progressItem) throws Exception {
		int totalParts = Mth.ceil((double) file.meta.size() / (double) chunk.length);
		VidLib.LOGGER.info("Uploading " + item + " (" + totalParts + " parts)");
		long offset = item.offset();
		long start = System.currentTimeMillis();

		if (offset >= file.meta.size()) {
			VidLib.LOGGER.info("Done uploading " + file.name() + " in " + (System.currentTimeMillis() - start) / 1000L + " s");
			return file;
		}

		try (var input = ProgressingInputStream.wrap(Files.newInputStream(file.path), progressItem)) {
			input.skipNBytes(offset);

			while (true) {
				int len = input.readNBytes(chunk, 0, (int) Math.min(file.meta.size() - offset, chunk.length));

				var response = HubAPI.HTTP_CLIENT.send(HubAPI.request(item.url(), false)
					.method("PATCH", HttpRequest.BodyPublishers.ofByteArray(chunk, 0, len))
					.header("Tus-Resumable", "1.0.0")
					.header("Content-Type", "application/offset+octet-stream")
					.header("Upload-Offset", Long.toUnsignedString(offset))
					.build(), HttpResponse.BodyHandlers.discarding());

				if (response.statusCode() / 100 == 2) {
					offset += len;

					var responseOffset = response.headers().firstValueAsLong("Upload-Offset").orElse(-1L);

					if (responseOffset != offset) {
						throw new IllegalStateException("Server reported back incorrect file offset " + responseOffset + ", expected " + offset);
					}

					if (totalParts > 1) {
						VidLib.LOGGER.info("Uploaded part " + Mth.ceil((double) offset / (double) chunk.length) + "/" + totalParts + " of " + file.name);
					}

					if (responseOffset >= file.meta.size()) {
						var fileId = response.headers().firstValue("X-File-ID").orElse("");

						if (!fileId.isEmpty()) {
							IOUtils.setAttribute(file.path, "MBG-Hub-Sync-ID", fileId);
						}

						VidLib.LOGGER.info("Done uploading " + file.name + " (" + fileId + ") in " + (System.currentTimeMillis() - start) / 1000L + " s");
						return file;
					}
				} else {
					throw new IllegalStateException("Server returned status code " + response.statusCode() + " uploading " + file.name);
				}
			}
		}
	}
}
