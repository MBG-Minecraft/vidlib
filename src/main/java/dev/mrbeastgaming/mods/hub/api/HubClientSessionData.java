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
import java.util.Optional;

public record HubClientSessionData(
	Optional<URI> gateway,
	HubUserData user,
	HubProjectData project,
	Optional<HubParticipantData> participant,
	HubUserCapabilities capabilities,
	Optional<HubMinecraftProfileData> minecraftProfile
) {
	public static HubClientSessionData CURRENT = null;

	public static final Codec<HubClientSessionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		KLibCodecs.URI.optionalFieldOf("gateway").forGetter(HubClientSessionData::gateway),
		HubUserData.CODEC.fieldOf("user").forGetter(HubClientSessionData::user),
		HubProjectData.CODEC.fieldOf("project").forGetter(HubClientSessionData::project),
		HubParticipantData.CODEC.optionalFieldOf("participant").forGetter(HubClientSessionData::participant),
		HubUserCapabilities.CODEC.fieldOf("capabilities").forGetter(HubClientSessionData::capabilities),
		HubMinecraftProfileData.CODEC.optionalFieldOf("minecraft_profile").forGetter(HubClientSessionData::minecraftProfile)
	).apply(instance, HubClientSessionData::new));

	public static void load() {
		var projectConfig = HubProjectConfig.INSTANCE.get();

		if (projectConfig == null || HubUserConfig.load().token().isEmpty()) {
			return;
		}

		VidLib.LOGGER.info("Loading Hub client session data...");
		CURRENT = null;

		try {
			var json = HubAPI.sendJsonRequest(HubAPI.apiProjectClientSession(projectConfig.token().encoded()));
			var data = CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
			VidLib.LOGGER.info("Loaded '" + data.project.toString() + "' as '" + data.user.toString() + "'");
			CURRENT = data;

			if (HubGateway.client == null && data.gateway.isPresent()) {
				HubGateway.client = new HubGateway(data.gateway.get());
				HubGateway.client.start();
			}
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to load Hub client session data: " + ex);
		}
	}

	public static void loadAsync() {
		Thread.startVirtualThread(HubClientSessionData::load);
	}

	public HubClientSessionData withUser(HubUserData user) {
		return new HubClientSessionData(gateway, user, project, participant, capabilities, minecraftProfile);
	}

	public HubClientSessionData withCapabilities(HubUserCapabilities capabilities) {
		return new HubClientSessionData(gateway, user, project, participant, capabilities, minecraftProfile);
	}
}
