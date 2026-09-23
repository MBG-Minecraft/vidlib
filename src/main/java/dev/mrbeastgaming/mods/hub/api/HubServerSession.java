package dev.mrbeastgaming.mods.hub.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import dev.mrbeastgaming.mods.hub.api.data.HubKeys;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import dev.mrbeastgaming.mods.hub.api.data.HubUser;
import dev.mrbeastgaming.mods.hub.api.gateway.HubServerGateway;
import dev.mrbeastgaming.mods.hub.file.UploadContext;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;

import java.nio.file.Files;
import java.util.HashSet;
import java.util.UUID;

public class HubServerSession {
	public UUID id = Util.NIL_UUID;
	public HubUser user = null;
	public HubProject project = null;
	public HubKeys keys = null;
	public HubKeys sessionKeys = null;
	public byte[] sessionSalt = new byte[0];
	public UploadContext uploadContext = null;

	public static HubServerSession CURRENT = null;

	public static void loadAsync(MinecraftServer server) {
		HubAPI.SEQUENTIAL_EXECUTOR.get().execute(() -> loadSync(server));
	}

	public static void loadSync(MinecraftServer server) {
		VidLib.LOGGER.info("Loading Hub server session data...");

		var session = new HubServerSession();

		try {
			var projectConfig = HubProjectConfig.INSTANCE.get();
			var token = projectConfig == null ? "" : projectConfig.token();

			var data = HubAPI.MinecraftAPI.postServerSession(new HubServerSessionRequest(
				server.isDedicatedServer(),
				token,
				new HubKeys(
					"RSA",
					server.getKeyPair().getPublic().getEncoded()
				)
			));

			session.id = data.id();
			session.user = data.ctx().user(data.user());
			session.project = data.ctx().project(data.project());
			session.keys = data.keys().orElse(null);
			session.sessionKeys = data.sessionKeys().orElse(null);
			session.sessionSalt = data.sessionSalt();
			session.uploadContext = token.isEmpty() || session.project == null ? null : new UploadContext(token, session.project);

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

			var gateway = HubServerGateway.startGateway(server, data.gateway().orElse(null), data.gatewayToken().orElse(""));

			if (gateway != null) {
				gateway.updateInfoFuture();
			}
		} catch (Exception ex) {
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
