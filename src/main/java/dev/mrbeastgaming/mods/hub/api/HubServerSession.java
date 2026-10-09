package dev.mrbeastgaming.mods.hub.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.mrbeastgaming.mods.hub.HubConfig;
import dev.mrbeastgaming.mods.hub.api.data.HubCountryList;
import dev.mrbeastgaming.mods.hub.api.data.HubKeys;
import dev.mrbeastgaming.mods.hub.api.gateway.HubServerGateway;
import dev.mrbeastgaming.mods.hub.file.HubUploadContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;

import java.nio.file.Files;
import java.util.HashSet;
import java.util.UUID;

public class HubServerSession extends HubCommonSession<HubServerGateway> {
	public static HubServerSession CURRENT = null;

	public static void loadAsync(MinecraftServer server) {
		HubAPI.SEQUENTIAL_EXECUTOR.get().execute(() -> loadSync(server));
	}

	public static void loadSync(MinecraftServer server) {
		VidLib.LOGGER.info("Loading Hub server session data...");

		var session = new HubServerSession();

		try {
			var projectToken = HubConfig.projectToken;

			var data = HubAPI.MinecraftAPI.postServerSession(new HubServerSessionRequest(
				server.isDedicatedServer(),
				projectToken,
				HubKeys.of(server.getKeyPair().getPublic()),
				HubConfig.identity
			));

			session.id = data.id();
			session.gatewayInfo = data.gatewayInfo().orElse(null);
			session.user = data.user().orElse(null);
			session.project = data.project().orElse(null);
			session.keys = data.keys();
			session.sessionKeys = data.sessionKeys();
			session.sessionSalt = data.sessionSalt();
			session.countries = HubCountryList.of(data.ctx().relevantCountries().values());
			session.country = data.country();

			session.uploadContext = projectToken.isEmpty() || session.project == null ? null : new HubUploadContext(
				projectToken,
				session.project,
				null,
				null,
				null
			);

			var userName = session.user == null ? "Public User" : session.user.toString();

			if (session.project != null) {
				VidLib.LOGGER.info("Server logged in '" + session.project.toString() + "' as '" + userName + "'");
			} else {
				VidLib.LOGGER.warn("Server logged in a misconfigured project as '" + userName + "'");
			}

			CURRENT = session;

			if (data.ops().isPresent()) {
				updateOps(server, data.ops().get().getAsJsonArray());
			}

			if (session.gateway != null) {
				session.gateway.stop();
				session.gateway = null;
			}

			session.gateway = HubServerGateway.startGateway(server, session.gatewayInfo);

			if (session.gateway != null) {
				session.gateway.updateInfoFuture();
			}

			HubProjectsResponse.ALL.forget();

			session.upload("Server Sync", uploads -> CommonGameEngine.INSTANCE.collectServerUploads(server, uploads)).whenComplete((unused, throwable) -> {
				if (throwable != null) {
					VidLib.LOGGER.error("Failed to sync server files", throwable);
				}
			});
		} catch (Exception ex) {
			HubProjectsResponse.ALL.forget();
			VidLib.LOGGER.error("Failed to load Hub server session data", ex);
		}
	}

	public static void updateOps(MinecraftServer server, JsonArray json) {
		var updated = new JsonArray();
		var existing = new HashSet<UUID>();

		for (var entry : json) {
			if (entry instanceof JsonObject o && o.has("hub") && o.has("uuid")) {
				try {
					var uuid = UndashedUuid.fromStringLenient(o.get("uuid").getAsString());

					if (existing.add(uuid)) {
						updated.add(o);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		var path = PlayerList.OPLIST_FILE.toPath();

		if (Files.exists(path)) {
			try {
				for (var entry : JsonUtils.read(path).getAsJsonArray()) {
					if (entry instanceof JsonObject o && !o.has("hub") && o.has("uuid")) {
						try {
							var uuid = UndashedUuid.fromStringLenient(o.get("uuid").getAsString());

							if (existing.add(uuid)) {
								updated.add(o);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		try {
			JsonUtils.write(path, updated, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		server.execute(() -> {
			try {
				server.getPlayerList().getOps().load();

				for (var player : server.getPlayerList().getPlayers()) {
					server.getPlayerList().sendPlayerPermissionLevel(player);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}
}
