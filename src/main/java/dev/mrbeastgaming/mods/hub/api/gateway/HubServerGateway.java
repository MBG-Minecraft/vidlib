package dev.mrbeastgaming.mods.hub.api.gateway;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.io.IOUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.misc.command.BackupCommand;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.HubServerSessionData;
import dev.mrbeastgaming.mods.hub.api.project.UsedPort;
import dev.mrbeastgaming.mods.hub.file.ChecksumPath;
import dev.mrbeastgaming.mods.hub.file.UploadRequestFile;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class HubServerGateway extends HubCommonGateway<MinecraftServer> {
	public static HubServerGateway instance;

	@Nullable
	public static HubServerGateway startGateway(MinecraftServer server, @Nullable URI uri, String token) {
		stopGateway();
		var gateway = instance;

		if (gateway == null && uri != null) {
			gateway = new HubServerGateway(server, uri, token);
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

	public static void updateInfo(MinecraftServer server, HubServerGateway gateway) {
		HubAPI.SEQUENTIAL_EXECUTOR.get().execute(() -> updateInfoFuture(server, gateway).join());
	}

	public static CompletableFuture<Void> updateInfoFuture(MinecraftServer server, HubServerGateway gateway) {
		var list = new ArrayList<CompletableFuture<Void>>();
		list.add(gateway.sendName(ChatFormatting.stripFormatting(server.getMotd().replace("\\n", "\n"))));
		list.add(gateway.sendSize(server.getPlayerCount()));
		list.add(gateway.sendStatus(CommonGameEngine.INSTANCE.getServerGatewayStatus(server)));
		list.add(gateway.sendUsedPorts());
		list.add(gateway.sendAvailableWorlds());
		return CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
	}

	public static JsonObject entityToJson(Entity entity) {
		var json = new JsonObject();
		json.addProperty("uuid", entity.getUUID().toString());
		json.addProperty("name", entity.getScoreboardName());
		json.addProperty("dimension", entity.level().dimension().location().toString());
		return json;
	}

	public static void playerLoggedIn(ServerPlayer player) {
		var gateway = HubServerGateway.instance;

		if (gateway != null) {
			var json = new JsonObject();
			json.add("player", entityToJson(player));
			gateway.send("player_logged_in", json);
			gateway.sendSize(player.server.getPlayerCount());
		}
	}

	public static void playerLoggedOut(ServerPlayer player, boolean offset) {
		var gateway = HubServerGateway.instance;

		if (gateway != null) {
			var json = new JsonObject();
			json.add("player", entityToJson(player));
			gateway.send("player_logged_out", json);
			gateway.sendSize(player.server.getPlayerCount() - (offset ? 1 : 0));
		}
	}

	public static void playerChangedDimension(ServerPlayer player, ResourceKey<Level> fromDim, ResourceKey<Level> toDim) {
		var gateway = HubServerGateway.instance;

		if (gateway != null) {
			var json = new JsonObject();
			json.add("player", entityToJson(player));
			json.addProperty("from_dimension", fromDim.location().toString());
			json.addProperty("to_dimension", toDim.location().toString());
			gateway.send("player_changed_dimension", json);
		}
	}

	public static void updateName(String name) {
		var gateway = HubServerGateway.instance;

		if (gateway != null) {
			gateway.sendName(name);
		}
	}

	public static void updateStatus(String status) {
		var gateway = HubServerGateway.instance;

		if (gateway != null) {
			gateway.sendStatus(status);
		}
	}

	public static void registerBuiltIn(HubGatewayEventRegistry<MinecraftServer> registry) {
		registry.registerSynced("request_restart", HubServerGateway::requestRestart);
		registry.registerSynced("run_command", HubServerGateway::runCommand);
		registry.registerSynced("update_ops", HubServerGateway::updateOps);
		registry.registerSynced("request_world_upload", HubServerGateway::requestWorldUpload);
	}

	private static void requestRestart(MinecraftServer server, HubGatewayEvent event) {
		server.halt(false);
	}

	private static void runCommand(MinecraftServer server, HubGatewayEvent event) {
		var command = event.paramsObject().get("command").getAsString();
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
	}

	private static void updateOps(MinecraftServer server, HubGatewayEvent event) {
		HubServerSessionData.updateOps(server, event.paramsArray());
	}

	private static void requestWorldUpload(MinecraftServer server, HubGatewayEvent event) {
		var data = HubWorldUploadRequestData.CODEC.parse(JsonOps.INSTANCE, event.params()).getOrThrow();

		var worlds = new ArrayList<HubWorldDirectory>(1);
		CommonGameEngine.INSTANCE.getAvailableWorlds(server, worlds);

		for (var world : worlds) {
			if (world.id().equals(data.worldId()) && world.path().equals(data.path())) {
				VidLib.LOGGER.warn(data.sendingTo().name() + " requested world " + data.path() + " upload");

				BackupCommand.backup(server, world.directory(), Instant.now(), "hub-upload").thenAcceptAsync(path -> {
					var progressItem = ProgressQueue.queueSingleItem("Uploading world...");

					try {
						VidLib.LOGGER.info("Uploading " + path + "...");
						var files = UploadRequestFile.loadDirectory("", path, null);
						event.gateway().upload(files, progressItem).join();
						HubAPI.MinecraftAPI.postCompleteWorldRequest(data.token(), files.stream().map(f -> new ChecksumPath(f.file().checksum(), f.file().id())).toList());
						VidLib.LOGGER.info("Cleaning up...");
					} catch (Exception ex) {
						VidLib.LOGGER.error("Error uploading world " + path + " to Hub", ex);
					} finally {
						try {
							IOUtils.deleteRecursively(path);
						} catch (Exception ex) {
							VidLib.LOGGER.error("Error deleting temp world " + path, ex);
						}

						progressItem.setDone();
					}
				}, Util.nonCriticalIoPool());
				return;
			}
		}
	}

	public final MinecraftServer server;

	public HubServerGateway(MinecraftServer server, URI gatewayURI, String gatewayToken) {
		super(server, gatewayURI, gatewayToken);
		this.server = server;
	}

	@Override
	public void collectEventHandlers(HubGatewayEventRegistry<MinecraftServer> registry) {
		PlatformHelper.CURRENT.collectServerGatewayEventHandlers(registry);
	}

	@Override
	public void onConnected() {
		super.onConnected();
		updateInfoFuture(main, this);
	}

	public CompletableFuture<Void> sendUsedPorts() {
		var usedPorts = new ArrayList<UsedPort>(3);
		CommonGameEngine.INSTANCE.getUsedPorts(server, usedPorts);
		return sendUsedPorts(usedPorts);
	}

	public CompletableFuture<Void> sendAvailableWorlds() {
		var availableWorlds = new ArrayList<HubWorldDirectory>(1);
		CommonGameEngine.INSTANCE.getAvailableWorlds(server, availableWorlds);
		return sendAvailableWorlds(availableWorlds);
	}
}
