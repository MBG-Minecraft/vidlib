package dev.beast.mods.shimmer.feature.worldsync;

import dev.beast.mods.shimmer.Shimmer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;

public class WorldSyncReadThread extends Thread {
	public final Minecraft mc;
	public final WorldSyncScreen screen;
	public final String ip;
	public final int port;
	private Path localPath;
	public boolean cancelled;
	public String serverPath, serverName;
	public int bufferSize;

	public WorldSyncReadThread(Minecraft mc, WorldSyncScreen screen, String ip, int port) {
		this.mc = mc;
		this.screen = screen;
		this.ip = ip;
		this.port = port;
		this.localPath = null;
		this.cancelled = false;
	}

	@Override
	public void run() {
		try {
			runClient();
		} catch (Exception ex) {
			ex.printStackTrace();
			sendMessage("Failed to sync the world: " + ex).red();
			cancel();
		}

		if (cancelled) {
			mc.execute(screen::onClose);
		} else {
			screen.createWorldButton.active = true;
			screen.cancelButton.setMessage(CommonComponents.GUI_BACK);
		}
	}

	private Socket createSocket() throws Exception {
		Socket socket = new Socket(ip, port);
		socket.setSoTimeout(30000);
		return socket;
	}

	private void runClient() throws Exception {
		long start = System.currentTimeMillis();

		sendMessage("Server found!");

		ProgressingText connectingText = sendMessage("Connecting to the server...");
		connectingText.maxProgress = 1L;

		try (Socket socket = createSocket();
			 DataOutputStream outInfo = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			 DataInputStream inInfo = new DataInputStream(new BufferedInputStream(socket.getInputStream()))
		) {
			connectingText.progress = 1L;
			connectingText.setText("Connecting to the server... Done!").green();
			outInfo.writeByte(NetworkUtils.SEND_INFO);
			outInfo.flush();

			serverName = inInfo.readUTF();
			serverPath = serverName.replaceAll("\\W", "_").replaceAll("_{2,}", "_");
			bufferSize = Math.clamp(NetworkUtils.readVarInt(inInfo), 1024, 2097152);

			sendMessage("Server Name: " + serverName);
			sendMessage("Server Path: " + serverPath);
			sendMessage("Buffer size: " + bufferSize);

			localPath = Shimmer.WORLD_SYNC_PATH.get().resolve(serverPath);

			if (Files.notExists(localPath)) {
				Files.createDirectories(localPath);
			}

			WorldIndex worldIndex = WorldIndex.load(localPath);

			int remoteFileCount = NetworkUtils.readVarInt(inInfo);
			long totalWorldSize = 0L;
			List<RemoteFile> remoteFileList = new ArrayList<>();

			for (int i = 0; i < remoteFileCount; i++) {
				String path = inInfo.readUTF();
				long size = NetworkUtils.readVarLong(inInfo);

				if (size > 0L) {
					totalWorldSize += size;
					remoteFileList.add(new RemoteFile(path, NetworkUtils.readUUID(inInfo), size, i));
				} else {
					remoteFileList.add(new RemoteFile(path, Util.NIL_UUID, 0L, i));
				}
			}

			Map<String, RemoteFile> remoteFiles = new HashMap<>(remoteFileList.size());

			for (RemoteFile file : remoteFileList) {
				remoteFiles.put(file.path(), file);
			}

			List<RemoteFile> requestList = new ArrayList<>();

			for (RemoteFile remoteFile : remoteFileList) {
				LocalFile localFile = worldIndex.files().get(remoteFile.path());

				if (localFile == null || localFile.size() != remoteFile.size() || !localFile.checksum().equals(remoteFile.checksum())) {
					if (remoteFile.size() > 0L) {
						requestList.add(remoteFile);
					}
				}
			}

			var executorService = Executors.newScheduledThreadPool(30);
			CompletableFuture<SaveFileTask>[] receiveFileTaskFutures = new CompletableFuture[requestList.size()];
			screen.progress = 0L;
			screen.maxProgress = 0L;

			for (int i = 0; i < requestList.size(); i++) {
				RemoteFile remoteFile = requestList.get(i);
				receiveFileTaskFutures[i] = CompletableFuture.supplyAsync(new ReceiveFileTask(this, remoteFile), executorService);
				screen.maxProgress += remoteFile.size();
			}

			sendMessage(String.format("Receiving %d files", requestList.size()));

			SaveFileTask[] saveFileTasks = CompletableFuture.allOf(receiveFileTaskFutures).thenApply(t -> Arrays.stream(receiveFileTaskFutures).map(CompletableFuture::join).toArray(SaveFileTask[]::new)).get();

			int successFileCount = 0;

			for (SaveFileTask task : saveFileTasks) {
				if (task.success()) {
					successFileCount++;
				}
			}

			if (successFileCount == saveFileTasks.length) {
				sendMessage(String.format("Received %d/%d files", successFileCount, saveFileTasks.length));
			} else {
				sendMessage(String.format("Received %d/%d files", successFileCount, saveFileTasks.length)).red();
			}

			CompletableFuture<Void>[] writeFileTaskFutures = new CompletableFuture[successFileCount];
			successFileCount = 0;
			screen.progress = 0L;
			screen.maxProgress = 0L;

			for (SaveFileTask task : saveFileTasks) {
				if (task.success()) {
					writeFileTaskFutures[successFileCount] = CompletableFuture.runAsync(task, executorService);
					successFileCount++;
					screen.maxProgress += task.receiveFileTask.remoteFile.size();
				}
			}

			CompletableFuture.allOf(writeFileTaskFutures).thenApply(t -> Arrays.stream(writeFileTaskFutures).map(CompletableFuture::join).toArray()).get();

			for (LocalFile localFile : worldIndex.files().values()) {
				if (!remoteFiles.containsKey(localFile.path())) {
					ProgressingText text = sendMessage("Deleting " + localFile.path() + "...");
					Util.backgroundExecutor().execute(new DeleteFileTask(text, localFile));
				}
			}

			executorService.shutdownNow();
			sendMessage("Writing index...");

			List<String> list = new ArrayList<>();
			list.add("# name " + serverName);
			list.add("# size " + totalWorldSize);
			list.add("");
			list.addAll(remoteFiles.values().stream().sorted(Comparator.comparing(RemoteFile::path)).map(RemoteFile::toString).toList());
			Files.write(localPath.resolve("index.txt"), list);
			sendMessage(String.format("Done in %.3f! Click Proceed to create or update local world", (System.currentTimeMillis() - start) / 1000D)).green();
		}

		screen.maxProgress = 1L;
		screen.progress = 1L;

		if (!cancelled) {
			try (Socket socket = createSocket();
				 DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))
			) {
				out.writeByte(NetworkUtils.STOP_SYNC);
				out.flush();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void cancel() {
		if (!cancelled) {
			cancelled = true;
			new Thread(this::cancelNow, "WorldSyncCancel").start();
		}
	}

	private void cancelNow() {
		try (Socket socket = createSocket();
			 DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))
		) {
			out.writeByte(NetworkUtils.CANCEL_SYNC);
			out.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private record ReceiveFileTask(WorldSyncReadThread worldSync, RemoteFile remoteFile) implements Supplier<SaveFileTask> {
		@Override
		public SaveFileTask get() {
			if (worldSync.cancelled) {
				return new SaveFileTask(this);
			}

			ProgressingText text = worldSync.sendMessage("Receiving " + remoteFile.path() + "...");

			try (Socket socket = worldSync.createSocket();
				 DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
				 DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))
			) {
				out.writeByte(NetworkUtils.SEND_FILE);
				NetworkUtils.writeVarInt(out, remoteFile.index());
				out.flush();

				boolean compressed = in.readBoolean();
				int bytesLeft = NetworkUtils.readVarInt(in);
				text.maxProgress = bytesLeft;

				byte[] tempBuffer = new byte[(int) Math.min(worldSync.bufferSize, bytesLeft)];
				ByteArrayOutputStream fileBytes = new ByteArrayOutputStream(bytesLeft);

				while (true) {
					int read = in.read(tempBuffer);

					if (read >= 0) {
						bytesLeft -= read;
						text.progress += read;
						fileBytes.write(tempBuffer, 0, read);

						if (worldSync.cancelled) {
							return new SaveFileTask(this);
						}
					} else {
						break;
					}
				}

				worldSync.screen.progress += remoteFile.size(); // TODO: Change this to use compressed size

				text.setText("Received " + remoteFile.path());
				return new SaveFileTask(this, fileBytes.toByteArray(), compressed, true);
			} catch (Exception ex) {
				ex.printStackTrace();
				text.setText("Failed to receive " + remoteFile.path() + ": " + ex.getMessage()).red();
				return new SaveFileTask(this);
			}
		}
	}

	private record SaveFileTask(ReceiveFileTask receiveFileTask, byte[] bytes, boolean compressed, boolean success) implements Runnable {
		public SaveFileTask(ReceiveFileTask receiveFileTask) {
			this(receiveFileTask, new byte[0], false, false);
		}

		@Override
		public void run() {
			Path path = receiveFileTask.worldSync.localPath.resolve(receiveFileTask.remoteFile.checksum().toString());
			ProgressingText text = receiveFileTask.worldSync.sendMessage("Saving " + receiveFileTask.remoteFile.path() + "...");
			text.maxProgress = receiveFileTask.remoteFile.size();

			try (OutputStream fileOutput = new BufferedOutputStream(Files.newOutputStream(path))) {
				InputStream byteInput = new ByteArrayInputStream(bytes);

				if (compressed) {
					byteInput = new GZIPInputStream(byteInput);
				}

				byte[] tempBuffer = new byte[(int) Math.min(receiveFileTask.worldSync.bufferSize, bytes.length)];

				while (true) {
					int read = byteInput.read(tempBuffer);

					if (read >= 0) {
						fileOutput.write(tempBuffer, 0, read);
						text.progress += read;
						receiveFileTask.worldSync.screen.progress += read;
					} else {
						break;
					}
				}

				text.setText("Saved " + receiveFileTask.remoteFile.path());
			} catch (IOException e) {
				receiveFileTask.worldSync.sendMessage("Failed to save file " + receiveFileTask.remoteFile.path() + " | " + receiveFileTask.remoteFile.checksum() + "!").red();
				throw new RuntimeException(e);
			}
		}
	}

	public record DeleteFileTask(ProgressingText text, LocalFile file) implements Runnable {
		@Override
		public void run() {
			try {
				if (Files.deleteIfExists(file.file())) {
					text.setText("Deleted file " + file.path() + " | " + file.checksum());
				}
			} catch (IOException e) {
				text.setText("Failed to delete file " + file.path() + " | " + file.checksum() + "!").red();
				throw new RuntimeException(e);
			}
		}
	}

	public ProgressingText sendMessage(String text) {
		ProgressingText t = new ProgressingText().setText(text);
		mc.submit(() -> screen.text.add(t));
		return t;
	}
}
