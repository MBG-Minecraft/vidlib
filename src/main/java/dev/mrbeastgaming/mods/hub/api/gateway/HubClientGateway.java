package dev.mrbeastgaming.mods.hub.api.gateway;

import dev.latvian.mods.klib.io.bytes.ByteInput;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.VLJoinMultiplayerScreen;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItem;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressItemNameFunction;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.mrbeastgaming.mods.hub.api.Auth;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubClientSession;
import dev.mrbeastgaming.mods.hub.api.data.HubGameServer;
import dev.mrbeastgaming.mods.hub.api.data.HubUserCapabilities;
import dev.mrbeastgaming.mods.hub.api.data.HubUserFlags;
import dev.mrbeastgaming.mods.hub.client.HubWorldsPanel;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class HubClientGateway extends HubCommonGateway<Minecraft> {
	private static HubClientGateway instance;

	public static final Map<UUID, ProgressItem> PROGRESS_BARS = new ConcurrentHashMap<>();

	@Nullable
	public static HubClientGateway get() {
		return instance;
	}

	public static void ifPresent(Consumer<HubClientGateway> consumer) {
		var gateway = get();

		if (gateway != null) {
			consumer.accept(gateway);
		}
	}

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

	public static void registerBuiltIn(HubGatewayEventRegistry<Minecraft> registry) {
		registerCommonBuiltIn(registry);
		registry.registerSynced("display_toast", HubClientGateway::displayToast);
		registry.register("flags_updated", HubClientGateway::flagsUpdated);
		registry.register("capabilities_updated", HubClientGateway::capabilitiesUpdated);
		registry.register("server_list_updated", HubClientGateway::serverListUpdated);
		// TODO: edit options
		// TODO: save replay
		// TODO: save voice recording
		// TODO: open url
	}

	private static void displayToast(Minecraft mc, HubGatewayEvent event) {
		var params = event.paramsObject();
		var title = params.get("title").getAsString();
		var subtitle = params.has("subtitle") ? params.get("subtitle").getAsString() : "";
		mc.toast(Component.literal(title), subtitle.isEmpty() ? Component.empty() : Component.literal(subtitle));
	}

	private static void flagsUpdated(Minecraft mc, HubGatewayEvent event) {
		var self = HubClientSession.CURRENT.user;

		if (self != null) {
			var flags = HubUserFlags.CODEC.parse(HubAPI.jsonOps(), event.params()).getOrThrow();
			HubClientSession.CURRENT.user = self.withFlags(flags);
		}
	}

	private static void capabilitiesUpdated(Minecraft mc, HubGatewayEvent event) {
		HubClientSession.CURRENT.capabilities = event.params() == null ? HubUserCapabilities.DEFAULT : HubUserCapabilities.CODEC.parse(HubAPI.jsonOps(), event.params()).getOrThrow();
	}

	private static void serverListUpdated(Minecraft mc, HubGatewayEvent event) {
		HubClientSession.CURRENT.servers = event.params() == null ? List.of() : HubGameServer.LIST_CODEC.parse(HubAPI.jsonOps(), event.params()).getOrThrow();

		mc.execute(() -> {
			if (mc.screen instanceof VLJoinMultiplayerScreen screen) {
				screen.vl$refresh();
			}
		});
	}

	private static void cutRecording(Minecraft mc, HubGatewayEvent event) {
		ReplayAPI.getActive().cutRecording();
	}

	public final Minecraft mc;

	public HubClientGateway(Minecraft mc, URI gatewayURI, String gatewayToken) {
		super(mc, gatewayURI, gatewayToken);
		this.mc = mc;
	}

	@Override
	public HubClientSession getHubSession() {
		return HubClientSession.CURRENT;
	}

	@Override
	public void requestRestart() {
		if (mc.level != null) {
			mc.vl$exitToTitle();
		}

		// Display GUI
		mc.stop();
	}

	@Override
	public void collectEventHandlers(HubGatewayEventRegistry<Minecraft> registry) {
		registerBuiltIn(registry);
		NeoForge.EVENT_BUS.post(new HubClientGatewayEventRegistryEvent(registry));
	}

	@Override
	public void onConnected() {
		super.onConnected();
		updateInfo();
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

	public void updateInfo() {
		HubAPI.SEQUENTIAL_EXECUTOR.get().execute(() -> updateInfoFuture().join());
	}

	public CompletableFuture<Void> updateInfoFuture() {
		var list = new ArrayList<CompletableFuture<Void>>();
		list.add(sendName());
		list.add(sendStatus());
		return CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
	}

	public CompletableFuture<Void> sendName() {
		return sendName(ClientGameEngine.INSTANCE.getClientGatewayName(main));
	}

	public CompletableFuture<Void> sendStatus() {
		return sendStatus(ClientGameEngine.INSTANCE.getClientGatewayStatus(main));
	}
}
