package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import dev.mrbeastgaming.mods.hub.api.gateway.HubServerGateway;
import dev.mrbeastgaming.mods.hub.api.project.HubProjectData;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public record HubServerSessionData(
	UUID sessionId,
	Optional<URI> gateway,
	HubUserData user,
	HubProjectData project,
	HubKeyData keys,
	HubKeyData sessionKeys,
	byte[] sessionSalt
) {
	public static final Codec<HubServerSessionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		KLibCodecs.UUID.fieldOf("session_id").forGetter(HubServerSessionData::sessionId),
		KLibCodecs.URI.optionalFieldOf("gateway").forGetter(HubServerSessionData::gateway),
		HubUserData.CODEC.fieldOf("user").forGetter(HubServerSessionData::user),
		HubProjectData.CODEC.fieldOf("project").forGetter(HubServerSessionData::project),
		HubKeyData.CODEC.fieldOf("keys").forGetter(HubServerSessionData::keys),
		HubKeyData.CODEC.fieldOf("session_keys").forGetter(HubServerSessionData::sessionKeys),
		KLibCodecs.B64_BYTE_ARRAY.fieldOf("session_salt").forGetter(HubServerSessionData::sessionSalt)
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
				projectConfig == null ? "" : projectConfig.token().encoded(),
				new HubKeyData(
					"RSA",
					server.getKeyPair().getPublic().getEncoded()
				)
			));

			VidLib.LOGGER.info("Logged in '" + data.project.toString() + "' as '" + data.user.toString() + "'");

			var gateway = HubServerGateway.startGateway(server, HubAPI.toWebSocketURI(data.gateway.orElse(null)));

			if (gateway != null) {
				gateway.sendName("Port " + server.getPort() + "\n" + ChatFormatting.stripFormatting(server.getMotd().replace("\\n", "\n")));
				gateway.sendStatus("0 Online");
			}
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to load Hub server session data", ex);
		}

		INSTANCE = data;
	}
}
