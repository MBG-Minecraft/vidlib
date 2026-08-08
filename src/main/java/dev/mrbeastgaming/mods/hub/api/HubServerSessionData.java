package dev.mrbeastgaming.mods.hub.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import dev.mrbeastgaming.mods.hub.api.gateway.HubServerGateway;
import dev.mrbeastgaming.mods.hub.api.project.HubProjectData;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.ExtraCodecs;

import java.net.URI;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public record HubServerSessionData(
	UUID sessionId,
	Optional<URI> gateway,
	HubUserData user,
	HubProjectData project,
	HubKeyData keys,
	HubKeyData sessionKeys,
	byte[] sessionSalt,
	Optional<JsonElement> ops
) {
	public static final Codec<HubServerSessionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		KLibCodecs.UUID.fieldOf("session_id").forGetter(HubServerSessionData::sessionId),
		KLibCodecs.URI.optionalFieldOf("gateway").forGetter(HubServerSessionData::gateway),
		HubUserData.CODEC.fieldOf("user").forGetter(HubServerSessionData::user),
		HubProjectData.CODEC.fieldOf("project").forGetter(HubServerSessionData::project),
		HubKeyData.CODEC.fieldOf("keys").forGetter(HubServerSessionData::keys),
		HubKeyData.CODEC.fieldOf("session_keys").forGetter(HubServerSessionData::sessionKeys),
		KLibCodecs.B64_BYTE_ARRAY.fieldOf("session_salt").forGetter(HubServerSessionData::sessionSalt),
		ExtraCodecs.JSON.optionalFieldOf("ops").forGetter(HubServerSessionData::ops)
	).apply(instance, HubServerSessionData::new));

	public static HubServerSessionData INSTANCE = null;

	public static void loadAsync(MinecraftServer server) {
		HubAPI.SEQUENTIAL_EXECUTOR.get().execute(() -> loadSync(server));
	}

	public static void loadSync(MinecraftServer server) {
		VidLib.LOGGER.info("Loading Hub server session data...");
		HubServerSessionData data = null;

		try {
			var projectConfig = HubProjectConfig.INSTANCE.get();

			data = HubAPI.apiServerSession(new HubServerSessionDataRequest(
				server.isDedicatedServer(),
				projectConfig == null ? "" : projectConfig.token().encoded(),
				new HubKeyData(
					"RSA",
					server.getKeyPair().getPublic().getEncoded()
				)
			));

			VidLib.LOGGER.info("Logged in '" + data.project.toString() + "' as '" + data.user.toString() + "'");

			if (data.ops.isPresent()) {
				updateOps(server, data.ops.get().getAsJsonArray());
			}

			var gateway = HubServerGateway.startGateway(server, HubAPI.toWebSocketURI(data.gateway.orElse(null)));

			if (gateway != null) {
				HubServerGateway.updateInfo(server, gateway);
			}
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to load Hub server session data", ex);
		}

		INSTANCE = data;
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
