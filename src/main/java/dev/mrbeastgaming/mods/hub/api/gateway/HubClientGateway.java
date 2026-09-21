package dev.mrbeastgaming.mods.hub.api.gateway;

import dev.latvian.mods.klib.io.bytes.ByteInput;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItem;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItemNameFunction;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.mrbeastgaming.mods.hub.api.Auth;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.client.HubWorldsPanel;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class HubClientGateway extends HubCommonGateway<Minecraft> {
	public static HubClientGateway instance;

	public static final Map<UUID, ProgressItem> PROGRESS_BARS = new ConcurrentHashMap<>();

	@Nullable
	public static HubClientGateway startGateway(Minecraft mc, @Nullable URI uri, String token) {
		stopGateway();
		var gateway = instance;

		if (gateway == null && uri != null) {
			gateway = new HubClientGateway(mc, uri, token);
			gateway.start();
			instance = gateway;
		}

		return gateway;
	}

	public static void stopGateway() {
		var gateway = instance;

		if (gateway != null) {
			gateway.stop();
			instance = null;
		}
	}

	public static void tickGateway() {
		var gateway = instance;

		if (gateway != null) {
			gateway.tick();
		}
	}

	public static void updateInfo(Minecraft mc, HubClientGateway gateway) {
		gateway.sendName(mc.getUser().getName());

		var server = mc.getCurrentServer();

		if (server != null) {
			gateway.sendStatus("Server - " + server.name);
		} else if (mc.level != null && PlatformHelper.CURRENT.isReplayLevel(mc.level)) {
			gateway.sendStatus("Replay Editor");
		} else if (mc.level != null) {
			gateway.sendStatus("Singleplayer");
		} else {
			gateway.sendStatus("Main Menu");
		}
	}

	public final Minecraft mc;

	public HubClientGateway(Minecraft mc, URI gatewayURI, String gatewayToken) {
		super(mc, gatewayURI, gatewayToken);
		this.mc = mc;
	}

	@Override
	public void collectEventHandlers(HubGatewayEventRegistry<Minecraft> registry) {
		NeoForge.EVENT_BUS.post(new HubClientGatewayEventRegistryEvent(registry));
	}

	@Override
	public void onConnected() {
		super.onConnected();
		updateInfo(main, this);
	}

	@Override
	protected void handleDisplayProgressBar(ByteBuffer buffer) throws Exception {
		var data = ByteInput.of(buffer);
		var uuid = data.readUUID();
		var title = data.readUTF();
		var size = data.readVarLong();
		var progressItem = PROGRESS_BARS.get(uuid);

		if (progressItem == null) {
			progressItem = ProgressQueue.queueSingleItem(title);
			PROGRESS_BARS.put(uuid, progressItem);
		}

		progressItem.setSize(size);
	}

	@Override
	protected void handleProgressBarStyle(ByteBuffer buffer) throws Exception {
		var data = ByteInput.of(buffer);
		var progressItem = PROGRESS_BARS.get(data.readUUID());

		if (progressItem == null) {
			return;
		}

		int flags = data.readVarInt();
		progressItem.setBlocksExit((flags & 1) != 0);
		progressItem.queue.hideInGame = (flags & 2) != 0;
		progressItem.queue.canCancel = (flags & 4) != 0;
		progressItem.queue.bottomText = data.readUTF();
		progressItem.setLabel(data.readUTF());
		progressItem.parseInfoText(data.readUTF());
	}

	@Override
	protected void handleProgressBar(ByteBuffer buffer) throws Exception {
		var data = ByteInput.of(buffer);
		var progressItem = PROGRESS_BARS.get(data.readUUID());

		if (progressItem == null) {
			return;
		}

		progressItem.setProgress(data.readVarLong());
	}

	@Override
	protected void handleRemoveProgressBar(ByteBuffer buffer) throws Exception {
		var data = ByteInput.of(buffer);
		var progressItem = PROGRESS_BARS.remove(data.readUUID());

		if (progressItem == null) {
			return;
		}

		var errorCount = data.readVarInt();

		for (int i = 0; i < errorCount; i++) {
			progressItem.error(data.readUTF());
		}

		progressItem.setDone();
	}

	@Override
	protected void handleDownloadWorld(ByteBuffer buffer) throws Exception {
		var data = ByteInput.of(buffer);
		int flags = data.readVarInt();
		var requestId = data.readUUID();
		var uniqueId = data.readUTF();
		var count = data.readVarInt();

		var fileDownloads = new HashMap<String, FileDownload>(count);

		var templateDir = PlatformHelper.CURRENT.getGameDirectory().resolve("saves-templates");
		var directory = templateDir.resolve(uniqueId);

		if (Files.notExists(directory)) {
			Files.createDirectories(directory);
		}

		for (int i = 0; i < count; i++) {
			var checksum = Checksum.read(data);
			var size = data.readVarLong();
			var path = data.readUTF();
			var url = data.readUTF();
			var filePath = directory.resolve(path);
			fileDownloads.put(path, new FileDownload(checksum, size, path, url, filePath));
		}

		Util.ioPool().execute(() -> {
			long totalSize = 0L;

			try {
				for (var entry : fileDownloads.entrySet()) {
					var file = entry.getValue();

					if (Files.exists(file.filePath()) && file.checksum().type().digest(file.filePath(), 0L, file.size(), null).equals(file.checksum())) {
						entry.setValue(new FileDownload(file.checksum(), 0L, file.path(), file.url(), file.filePath()));
					} else {
						totalSize += file.size();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			var progressItem = PROGRESS_BARS.get(requestId);

			if (progressItem != null) {
				progressItem.setLabel("Downloading Template...");
				progressItem.setInfoText(ProgressItemNameFunction.BINARY_BYTE_SIZE);
				progressItem.resetProgress();
				progressItem.setSize(totalSize);
				progressItem.setStarted();
			}

			try (var stream = Files.walk(directory)) {
				for (var file : stream.filter(Files::isRegularFile).toList()) {
					if (!fileDownloads.containsKey(directory.relativize(file).toString())) {
						Files.delete(file);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try (var executor = Executors.newFixedThreadPool(20)) {
				for (var file : fileDownloads.values()) {
					var dir = file.filePath().getParent();

					if (Files.notExists(dir)) {
						Files.createDirectories(dir);
					}
				}

				var list = new ArrayList<CompletableFuture<Void>>();

				for (var file : fileDownloads.values()) {
					if (file.size() == 0L) {
						continue;
					}

					list.add(CompletableFuture.runAsync(() -> {
						var item = progressItem == null ? null : progressItem.queue.addItem();

						if (item != null) {
							item.setInfoText(file.path());
							item.setSize(file.size());
							item.setStarted();
							progressItem.queue.display();
						}

						try {
							HubAPI.download(HubAPI.request(file.url(), Auth.NOT_REQUIRED).build(), file.filePath());

							if (progressItem != null) {
								progressItem.addProgress(file.size());
							}
						} catch (Exception ex) {
							VidLib.LOGGER.error("Failed to download " + file.path(), ex);
						} finally {
							if (item != null) {
								item.setDone();
							}
						}
					}, executor));
				}

				CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
			} catch (Exception ex) {
				VidLib.LOGGER.error("Error while downloading world " + uniqueId, ex);

				if (progressItem != null) {
					progressItem.error(ex.toString());
				}
			} finally {
				PROGRESS_BARS.remove(requestId);

				if (progressItem != null) {
					progressItem.setDone();
				}

				HubWorldsPanel.INSTANCE.reload = true;
			}
		});
	}
}
