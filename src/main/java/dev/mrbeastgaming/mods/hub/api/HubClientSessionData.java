package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import dev.mrbeastgaming.mods.hub.HubUserConfig;
import dev.mrbeastgaming.mods.hub.api.gateway.HubGateway;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public record HubClientSessionData(
	Optional<URI> gateway,
	HubUserData user,
	Optional<HubProjectData> project,
	Optional<HubParticipantData> participant,
	HubUserCapabilities capabilities,
	Optional<HubMinecraftProfileData> minecraftProfile,
	List<HubGameServerData> servers
) {
	public static final Codec<HubClientSessionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		KLibCodecs.URI.optionalFieldOf("gateway").forGetter(HubClientSessionData::gateway),
		HubUserData.CODEC.fieldOf("user").forGetter(HubClientSessionData::user),
		HubProjectData.CODEC.optionalFieldOf("project").forGetter(HubClientSessionData::project),
		HubParticipantData.CODEC.optionalFieldOf("participant").forGetter(HubClientSessionData::participant),
		HubUserCapabilities.CODEC.fieldOf("capabilities").forGetter(HubClientSessionData::capabilities),
		HubMinecraftProfileData.CODEC.optionalFieldOf("minecraft_profile").forGetter(HubClientSessionData::minecraftProfile),
		HubGameServerData.LIST_CODEC.optionalFieldOf("servers", List.of()).forGetter(HubClientSessionData::servers)
	).apply(instance, HubClientSessionData::new));

	public static void load() {
		var projectConfig = HubProjectConfig.INSTANCE.get();

		if (HubUserConfig.load().token().isEmpty()) {
			return;
		}

		VidLib.LOGGER.info("Loading Hub client session data...");
		HubUserData userData = null;
		HubProjectData projectData = null;
		HubParticipantData participantData = null;
		HubUserCapabilities userCapabilities = HubUserCapabilities.DEFAULT;
		HubMinecraftProfileData minecraftProfileData = null;
		List<HubGameServerData> servers = List.of();

		try {
			var json = HubAPI.sendJsonRequest(HubAPI.apiDesktopClientSession(projectConfig == null ? "" : projectConfig.token().encoded()));
			var data = CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
			userData = data.user;
			projectData = data.project.orElse(null);
			participantData = data.participant.orElse(null);
			userCapabilities = data.capabilities;
			minecraftProfileData = data.minecraftProfile.orElse(null);
			servers = List.copyOf(data.servers);

			VidLib.LOGGER.info("Loaded '" + data.project.toString() + "' as '" + data.user.toString() + "'");

			if (HubGateway.client == null && data.gateway.isPresent()) {
				HubGateway.client = new HubGateway(data.gateway.get());
				HubGateway.client.start();
			}
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to load Hub client session data: " + ex);
		}

		HubUserData.SELF = userData;
		HubProjectData.PACK = projectData;
		HubParticipantData.SELF = participantData;
		HubUserCapabilities.CURRENT = userCapabilities;
		HubMinecraftProfileData.SELF = minecraftProfileData;
		HubGameServerData.CURRENT = servers;
	}
}
