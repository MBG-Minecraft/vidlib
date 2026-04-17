package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.klib.io.FileMD5;
import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.util.MD5;
import dev.latvian.mods.klib.util.Tristate;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItem;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItemNameFunction;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressingInputStream;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

public class HubFileUploads {
	public record SyncedFile(FileInfo fileInfo, FileMD5 meta, Mutable<ProgressItem> progressItem, ProjectUploadRequestItem item) {
	}

	public static List<SyncedFile> syncDirectory(Path directory, Consumer<HubFileUploadBuilder> upload) {
		var projectConfig = HubProjectConfig.INSTANCE.get();

		if (projectConfig == null || Files.notExists(directory)) {
			return List.of();
		}

		var uploadBuilder = new HubFileUploadBuilder();
		upload.accept(uploadBuilder);

		try (var stream = Files.walk(directory)) {
			var fileStream = stream
				.filter(Files::isRegularFile)
				.filter(Files::isReadable)
				.map(path -> new FileInfo(directory, path));

			if (uploadBuilder.filter != null) {
				fileStream = fileStream.filter(uploadBuilder::testFilter);
			}

			if (uploadBuilder.fileNameProvider != null) {
				fileStream = fileStream.map(fileInfo -> {
					try {
						var name = uploadBuilder.fileNameProvider.getFileName(fileInfo, projectConfig);

						if (name != null) {
							return new FileInfo(fileInfo.path(), name, fileInfo.size());
						}
					} catch (Exception ignored) {
					}

					return fileInfo;
				});
			}

			var fileList = fileStream.toList();
			return fileList.isEmpty() ? List.of() : syncFiles(projectConfig, fileList, uploadBuilder);
		} catch (Exception ex) {
			return List.of();
		}
	}

	public static List<SyncedFile> syncFile(Path file, Consumer<HubFileUploadBuilder> upload) {
		var projectConfig = HubProjectConfig.INSTANCE.get();

		if (projectConfig == null || Files.notExists(file)) {
			return List.of();
		}

		var fileInfo = new FileInfo(file);
		var uploadBuilder = new HubFileUploadBuilder();
		upload.accept(uploadBuilder);

		if (uploadBuilder.testFilter(fileInfo)) {
			if (uploadBuilder.fileNameProvider != null) {
				try {
					var name = uploadBuilder.fileNameProvider.getFileName(fileInfo, projectConfig);

					if (name != null) {
						fileInfo = new FileInfo(fileInfo.path(), name, fileInfo.size());
					}
				} catch (Exception ignored) {
				}
			}

			return syncFiles(projectConfig, List.of(fileInfo), uploadBuilder);
		}

		return List.of();
	}

	private static List<SyncedFile> syncFiles(HubProjectConfig projectConfig, List<FileInfo> fileList, HubFileUploadBuilder upload) {
		var resultFiles = new ArrayList<SyncedFile>(fileList.size());
		var progressItems = new ArrayList<ProgressItem>(fileList.size());
		var map = new LinkedHashMap<MD5, SyncedFile>();

		try {
			if (upload.progressQueue != null) {
				upload.progressQueue.topText = "Checking files...";

				for (var file : fileList) {
					var progressItem = upload.progressQueue.addItem(file.name(), ProgressItemNameFunction.SI_BYTE_SIZE);
					progressItem.setSize(file.size());
					progressItems.add(progressItem);
				}

				upload.progressQueue.display();
			}

			for (int i = 0; i < fileList.size(); i++) {
				var file = fileList.get(i);
				var progressItem = upload.progressQueue == null ? null : progressItems.get(i);

				try {
					if (progressItem != null) {
						progressItem.setStarted();
					}

					var uniqueId = upload.uniqueIdProvider == null ? MD5.NIL : upload.uniqueIdProvider.getUniqueId(file, projectConfig);

					if (uniqueId == null) {
						continue;
					}

					var meta = FileMD5.loadChanged(file, progressItem);

					if (meta == null) {
						continue;
					}

					VidLib.LOGGER.info("Updated metadata of " + file.name() + ": " + meta);

					var fileType = upload.type == null ? FileTypeProvider.probe(file) : upload.type.getFileType(file);

					var syncFile = new SyncedFile(file, meta, new MutableObject<>(), new ProjectUploadRequestItem(
						uniqueId,
						meta.checksum(),
						meta.size(),
						file.name(),
						fileType,
						upload.assignedTo,
						upload.assignedToMinecraft
					));

					map.put(syncFile.item.checksum(), syncFile);
				} catch (Exception ex) {
					VidLib.LOGGER.error("Failed to sync Beast Hub file " + file.name(), ex);
				} finally {
					if (progressItem != null) {
						progressItem.setDone();
					}
				}
			}

			if (upload.progressQueue != null) {
				upload.progressQueue.clear();
				progressItems.clear();
				upload.progressQueue.topText = "Uploading files...";
			}

			if (!map.isEmpty()) {
				var list = HubAPI.apiProjectUpload(projectConfig.token().toString(), map.values().stream().map(SyncedFile::item).toList());

				if (upload.progressQueue != null) {
					for (var item : list) {
						var syncFile = map.get(item.checksum());

						if (syncFile != null) {
							var progressItem = upload.progressQueue.addItem(syncFile.fileInfo().name(), ProgressItemNameFunction.SI_BYTE_SIZE);
							progressItem.setSize(syncFile.meta.size());
							syncFile.progressItem.setValue(progressItem);
						}
					}

					upload.progressQueue.display();
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

							VidLib.LOGGER.error("Failed to sync Beast Hub file " + syncFile.fileInfo.name(), ex);
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
			if (upload.progressQueue != null) {
				upload.progressQueue.topText = "Files Uploaded";
				upload.progressQueue.bottomText = "";

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
			VidLib.LOGGER.info("Done uploading " + file.fileInfo.name() + " in " + (System.currentTimeMillis() - start) / 1000L + " s");
			return file;
		}

		try (var input = ProgressingInputStream.wrap(Files.newInputStream(file.fileInfo.path()), progressItem)) {
			input.skipNBytes(offset);

			while (true) {
				int len = input.readNBytes(chunk, 0, (int) Math.min(file.meta.size() - offset, chunk.length));

				var response = HubAPI.HTTP_CLIENT.send(HubAPI.request(item.url(), Tristate.FALSE)
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
						VidLib.LOGGER.info("Uploaded part " + Mth.ceil((double) offset / (double) chunk.length) + "/" + totalParts + " of " + file.fileInfo.name());
					}

					if (responseOffset >= file.meta.size()) {
						var fileId = response.headers().firstValue("X-File-ID").orElse("");

						if (!fileId.isEmpty()) {
							IOUtils.setAttribute(file.fileInfo.path(), "MBG-Hub-Sync-ID", fileId);
						}

						VidLib.LOGGER.info("Done uploading " + file.fileInfo.name() + " (" + fileId + ") in " + (System.currentTimeMillis() - start) / 1000L + " s");
						return file;
					}
				} else {
					throw new IllegalStateException("Server returned status code " + response.statusCode() + " uploading " + file.fileInfo.name());
				}
			}
		}
	}
}
