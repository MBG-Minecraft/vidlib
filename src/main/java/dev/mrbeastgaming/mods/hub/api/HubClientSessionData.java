package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.StringUtils;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import dev.mrbeastgaming.mods.hub.HubUserConfig;
import dev.mrbeastgaming.mods.hub.api.gateway.HubGateway;
import dev.mrbeastgaming.mods.hub.api.project.HubParticipantData;
import dev.mrbeastgaming.mods.hub.api.project.HubProjectData;
import dev.mrbeastgaming.mods.hub.api.project.HubProjectsData;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

public record HubClientSessionData(
	Optional<URI> gateway,
	HubUserData user,
	Optional<HubProjectData> project,
	Optional<HubParticipantData> participant,
	HubUserCapabilities capabilities,
	HubKeyData keys,
	HubKeyData sessionKeys,
	byte[] sessionSalt,
	List<HubGameServerData> servers
) {
	public static final Codec<HubClientSessionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		KLibCodecs.URI.optionalFieldOf("gateway").forGetter(HubClientSessionData::gateway),
		HubUserData.CODEC.fieldOf("user").forGetter(HubClientSessionData::user),
		HubProjectData.CODEC.optionalFieldOf("project").forGetter(HubClientSessionData::project),
		HubParticipantData.CODEC.optionalFieldOf("participant").forGetter(HubClientSessionData::participant),
		HubUserCapabilities.CODEC.fieldOf("capabilities").forGetter(HubClientSessionData::capabilities),
		HubKeyData.CODEC.fieldOf("keys").forGetter(HubClientSessionData::keys),
		HubKeyData.CODEC.fieldOf("session_keys").forGetter(HubClientSessionData::sessionKeys),
		KLibCodecs.B64_BYTE_ARRAY.fieldOf("session_salt").forGetter(HubClientSessionData::sessionSalt),
		HubGameServerData.LIST_CODEC.optionalFieldOf("servers", List.of()).forGetter(HubClientSessionData::servers)
	).apply(instance, HubClientSessionData::new));

	public static String AUTH_SERVER_ID = "";

	@Nullable
	public static HubClientSessionData load(@Nullable HubUserConfig userConfig, @Nullable HubProjectConfig projectConfig) {
		if (userConfig == null || userConfig.token().orElse(null) == null) {
			return null;
		}

		VidLib.LOGGER.info("Loading Hub client session data...");
		HubClientSessionData data = null;
		HubUserData userData = null;
		HubProjectData projectData = null;
		HubParticipantData participantData = null;
		HubUserCapabilities userCapabilities = HubUserCapabilities.DEFAULT;
		List<HubGameServerData> servers = List.of();
		HubKeyData keys = null;
		HubKeyData sessionKeys = null;
		var authServerId = "";

		try {
			data = HubAPI.apiDesktopClientSession(new HubClientSessionDataRequest(projectConfig == null ? "" : projectConfig.token().encoded(), true));

			userData = data.user;
			projectData = data.project.orElse(null);
			participantData = data.participant.orElse(null);
			userCapabilities = data.capabilities;
			servers = List.copyOf(data.servers);
			keys = data.keys;
			sessionKeys = data.sessionKeys;

			try (var out = new ByteArrayOutputStream()) {
				out.write(data.sessionSalt());
				out.write(userConfig.token().get().encoded().getBytes(StandardCharsets.ISO_8859_1));
				authServerId = StringUtils.toHex(MessageDigest.getInstance("SHA-1").digest(out.toByteArray()));
			}

			if (projectData != null) {
				VidLib.LOGGER.info("Logged in '" + projectData.toString() + "' as '" + userData.toString() + "'");
			} else {
				VidLib.LOGGER.warn("Logged in a misconfigured project as '" + userData.toString() + "'");
			}

			var gateway = HubGateway.client;

			if (gateway == null && data.gateway.isPresent()) {
				gateway = new HubGateway(data.gateway.get());
				gateway.start();
				HubGateway.client = gateway;
			}
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to load Hub client session data: " + ex);
		}

		HubUserData.SELF = userData;
		HubProjectData.PACK = projectData;
		HubParticipantData.SELF = participantData;
		HubUserCapabilities.CURRENT = userCapabilities;
		HubGameServerData.CURRENT = servers;
		HubKeyData.KEYS = keys;
		HubKeyData.SESSION_KEYS = sessionKeys;
		AUTH_SERVER_ID = authServerId;

		HubProjectsData.ALL.forget();
		return data;
	}
}
