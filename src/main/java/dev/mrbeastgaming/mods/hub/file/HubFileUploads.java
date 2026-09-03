package dev.mrbeastgaming.mods.hub.file;

import dev.latvian.mods.klib.io.FileInfo;
import dev.latvian.mods.klib.io.FileMD5;
import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.klib.util.MD5;
import dev.latvian.mods.klib.util.StringUtils;
import dev.latvian.mods.klib.util.Tristate;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItem;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItemNameFunction;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubFileType;
import dev.mrbeastgaming.mods.hub.api.project.ProjectUploadRequestItem;
import dev.mrbeastgaming.mods.hub.api.project.ProjectUploadResponseItem;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HubFileUploads {
	public record SyncedFile(FileInfo fileInfo, FileMD5 meta, Mutable<ProgressItem> progressItem, ProjectUploadRequestItem item) {
	}

	public record Entry(FileInfo file, HubUploadBuilderBase upload) {
	}

	public static List<Entry> prepareDirectory(Path directory, Consumer<HubDirectoryUploadBuilder> upload) {
		if (Files.notExists(directory)) {
			return List.of();
		}

		var uploadBuilder = new HubDirectoryUploadBuilder();
		upload.accept(uploadBuilder);

		try (var stream = Files.walk(directory)) {
			var fileStream = stream
				.filter(Files::isRegularFile)
				.filter(Files::isReadable)
				.map(path -> new FileInfo(directory, path))
				.filter(fileInfo -> fileInfo.size() > 0L);

			if (uploadBuilder.filter != null) {
				fileStream = fileStream.filter(uploadBuilder::testFilter);
			}

			if (uploadBuilder.fileNameProvider != null) {
				fileStream = fileStream.map(fileInfo -> {
					try {
						var name = uploadBuilder.fileNameProvider.getFileName(fileInfo);

						if (name != null) {
							return new FileInfo(fileInfo.path(), name, fileInfo.size());
						}
					} catch (Exception ignored) {
					}

					return fileInfo;
				});
			}

			var fileList = fileStream.map(fileInfo -> new Entry(fileInfo, uploadBuilder)).toList();
			return fileList.isEmpty() ? List.of() : fileList;
		} catch (Exception ex) {
			return List.of();
		}
	}

	public static List<Entry> prepareFile(Path file, BiConsumer<FileInfo, HubFileUploadBuilder> upload) {
		if (Files.notExists(file)) {
			return List.of();
		}

		var fileInfo = new FileInfo(file);
		var uploadBuilder = new HubFileUploadBuilder();
		upload.accept(fileInfo, uploadBuilder);

		if (uploadBuilder.fileNameProvider != null) {
			try {
				var fileName = uploadBuilder.fileNameProvider.getFileName(fileInfo);

				if (fileName != null) {
					fileInfo = new FileInfo(fileInfo.path(), fileName, fileInfo.size());
				}
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to create a custom file name of " + file, ex);
			}
		}

		return List.of(new Entry(fileInfo, uploadBuilder));
	}

	public static List<SyncedFile> syncFiles(List<Entry> fileList, @Nullable ProgressQueue progressQueue) {
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

				for (var entry : fileList) {
					var file = entry.file;
					var progressItem = progressQueue.addItem(file.name(), ProgressItemNameFunction.SI_BYTE_SIZE);
					progressItem.blocksExit = true;
					progressItem.setSize(file.size());
					progressItems.add(progressItem);
				}

				progressQueue.display();
			}

			HubFileType commonType = HubFileType.UNKNOWN;

			for (int i = 0; i < fileList.size(); i++) {
				var entry = fileList.get(i);
				var file = entry.file;
				var upload = entry.upload;
				var progressItem = progressQueue == null ? null : progressItems.get(i);

				try {
					if (progressItem != null) {
						progressItem.label = file.name();
						progressItem.setStarted();
					}

					var uniqueId = upload.getUniqueId(file, projectConfig);

					if (uniqueId == null) {
						continue;
					}

					var fileType = upload.getFileType(file);

					if (commonType == HubFileType.UNKNOWN) {
						commonType = fileType;
					} else if (commonType != null && commonType != fileType) {
						commonType = null;
					}

					var meta = FileMD5.load(file, progressItem);

					if (meta.changed()) {
						FileMD5.save(file.path(), meta);
						Files.setLastModifiedTime(file.path(), FileTime.from(meta.lastModified()));
						VidLib.LOGGER.info("Updated metadata of " + file.name() + ": " + meta);
					}

					var created = upload.getFileCreated(file);

					var syncFile = new SyncedFile(file, meta, new MutableObject<>(), new ProjectUploadRequestItem(
						uniqueId,
						meta.checksum(),
						meta.size(),
						file.name(),
						fileType,
						created,
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

			if (progressQueue != null) {
				progressQueue.clear();
				progressItems.clear();
				progressQueue.topText = "Uploading files...";
			}

			var customName = commonType != null && commonType != HubFileType.UNKNOWN ? commonType.name() : "";

			if (customName.isEmpty()) {
				VidLib.LOGGER.info("Checking Beast Hub uploads (" + map.size() + ") " + map.values().stream().map(SyncedFile::fileInfo).map(FileInfo::name).collect(Collectors.joining(", ", "[", "]")));
			} else {
				VidLib.LOGGER.info("Checking " + customName + " Beast Hub uploads (" + map.size() + ")");
			}

			if (!map.isEmpty()) {
				var list = HubAPI.apiProjectUpload(projectConfig.token().toString(), map.values().stream().map(SyncedFile::item).toList());
				VidLib.LOGGER.info("Uploading " + list.size() + " files to Beast Hub");

				for (var item : list) {
					VidLib.LOGGER.info("- " + item.toString() + ": " + item.url());
				}

				if (progressQueue != null) {
					for (var item : list) {
						var syncFile = map.get(item.checksum());

						if (syncFile != null) {
							var progressItem = progressQueue.addItem(syncFile.fileInfo().name(), ProgressItemNameFunction.SI_BYTE_SIZE);
							progressItem.blocksExit = true;
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
							progressItem.label = syncFile.fileInfo.name();
							progressItem.setStarted();
						}

						try {
							if (chunk == null) {
								long maxSize = 0L;

								for (var item1 : map.values()) {
									maxSize = Math.max(maxSize, item1.meta.size());
								}

								chunk = new byte[(int) Math.min(maxSize, item.maxChunkSize())];
							}

							resultFiles.add(syncFile1(syncFile, item, chunk, progressItem));
						} catch (Exception ex) {
							if (progressItem != null) {
								progressItem.error(ex.getMessage());
							}

							VidLib.errorToHub("Failed to sync Beast Hub file " + syncFile.fileInfo.name(), ex);
						} finally {
							if (progressItem != null) {
								progressItem.setDone();
							}
						}
					}
				}
			} else {
				VidLib.LOGGER.info("All files are up to date");
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
		var name = file.fileInfo.name();

		if (offset >= file.meta.size()) {
			VidLib.LOGGER.info("Done uploading " + name + " in " + (System.currentTimeMillis() - start) / 1000L + " s");
			return file;
		}

		try (var fileInputStream = Files.newInputStream(file.fileInfo.path())) {
			fileInputStream.skipNBytes(offset);

			while (true) {
				int len = fileInputStream.readNBytes(chunk, 0, (int) Math.min(file.meta.size() - offset, chunk.length));
				var fullChunkString = StringUtils.siByteSize(offset) + " - " + StringUtils.siByteSize(offset + len) + " | " + Mth.ceil((double) offset / (double) chunk.length) + "/" + totalParts;

				if (progressItem != null) {
					progressItem.setInfoText("Connecting...");
				}

				var request = HubAPI.request(item.url(), Tristate.FALSE).build();
				var connection = (HttpURLConnection) request.uri().toURL().openConnection();
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setChunkedStreamingMode(32768);
				connection.setRequestMethod("PUT");
				connection.setRequestProperty("Tus-Resumable", "1.0.0");
				connection.setRequestProperty("Content-Type", "application/offset+octet-stream");
				connection.setRequestProperty("Upload-Offset", Long.toUnsignedString(offset));
				connection.setRequestProperty("X-Content-Length-Hint", Long.toUnsignedString(len));
				connection.setRequestProperty("Transfer-Encoding", "chunked");

				if (progressItem != null) {
					progressItem.setInfoText(ProgressItemNameFunction.SI_BYTE_SIZE);
				}

				try (var out = connection.getOutputStream()) {
					int remaining = len;
					int index = 0;

					while (remaining > 0) {
						int sent = Math.min(remaining, 32768);
						out.write(chunk, index, sent);
						remaining -= sent;
						index += sent;
						out.flush();

						if (progressItem != null) {
							progressItem.addProgress(sent);
						}
					}
				}

				if (progressItem != null) {
					progressItem.setInfoText("Processing...");
				}

				int responseCode = connection.getResponseCode();

				if (responseCode / 100 == 2) {
					offset += len;

					long responseOffset = Optional.ofNullable(connection.getHeaderField("Upload-Offset")).map(Long::parseUnsignedLong).orElse(-1L);

					if (responseOffset != offset) {
						throw new IllegalStateException("Server reported back incorrect file offset " + responseOffset + ", expected " + offset);
					}

					if (totalParts > 1) {
						VidLib.LOGGER.info("Uploaded part " + fullChunkString + " of " + name);
					}

					if (responseOffset >= file.meta.size()) {
						var fileId = Optional.ofNullable(connection.getHeaderField("X-File-ID")).orElse("");

						if (!fileId.isEmpty()) {
							IOUtils.setAttribute(file.fileInfo.path(), "MBG-Hub-Sync-ID", fileId);
						}

						VidLib.LOGGER.info("Done uploading " + name + " (" + fileId + ") in " + (System.currentTimeMillis() - start) / 1000L + " s");
						return file;
					}
				} else {
					try (var in = connection.getInputStream()) {
						var json = JsonUtils.read(in).getAsJsonObject();
						var message = json.get("message").getAsString();
						throw new IllegalStateException("Error " + responseCode + " uploading " + name + " (" + fullChunkString + ")" + ": " + message);
					} catch (Exception ignored) {
						throw new IllegalStateException("Error " + responseCode + " uploading " + name + " (" + fullChunkString + ")");
					}
				}

				connection.disconnect();
			}
		}
	}
}
