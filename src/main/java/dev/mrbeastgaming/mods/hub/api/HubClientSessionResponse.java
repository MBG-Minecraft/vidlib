package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.mrbeastgaming.mods.hub.api.data.HubGameServer;
import dev.mrbeastgaming.mods.hub.api.data.HubKeys;
import dev.mrbeastgaming.mods.hub.api.data.HubParticipant;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import dev.mrbeastgaming.mods.hub.api.data.HubResponseContext;
import dev.mrbeastgaming.mods.hub.api.data.HubUser;
import dev.mrbeastgaming.mods.hub.api.data.HubUserCapabilities;
import net.minecraft.Util;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record HubClientSessionResponse(
	HubResponseContext ctx,
	UUID id,
	Optional<URI> gateway,
	Optional<String> gatewayToken,
	Optional<HubUser> user,
	Optional<HubProject> project,
	Optional<HubParticipant> participant,
	HubUserCapabilities capabilities,
	Optional<HubKeys> keys,
	Optional<HubKeys> sessionKeys,
	byte[] sessionSalt,
	List<HubGameServer> servers
) {
	public static final Codec<HubClientSessionResponse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubResponseContext.MAP_CODEC.forGetter(HubClientSessionResponse::ctx),
		KLibCodecs.UUID.optionalFieldOf("id", Util.NIL_UUID).forGetter(HubClientSessionResponse::id),
		HubAPI.WS_URI_BASE_CODEC.optionalFieldOf("gateway").forGetter(HubClientSessionResponse::gateway),
		Codec.STRING.optionalFieldOf("gateway_token").forGetter(HubClientSessionResponse::gatewayToken),
		HubUser.CODEC.optionalFieldOf("user").forGetter(HubClientSessionResponse::user),
		HubProject.CODEC.optionalFieldOf("project").forGetter(HubClientSessionResponse::project),
		HubParticipant.CODEC.optionalFieldOf("participant").forGetter(HubClientSessionResponse::participant),
		HubUserCapabilities.CODEC.optionalFieldOf("capabilities", HubUserCapabilities.DEFAULT).forGetter(HubClientSessionResponse::capabilities),
		HubKeys.CODEC.optionalFieldOf("keys").forGetter(HubClientSessionResponse::keys),
		HubKeys.CODEC.optionalFieldOf("session_keys").forGetter(HubClientSessionResponse::sessionKeys),
		KLibCodecs.B64_BYTE_ARRAY.optionalFieldOf("session_salt", new byte[0]).forGetter(HubClientSessionResponse::sessionSalt),
		HubGameServer.LIST_CODEC.optionalFieldOf("servers", List.of()).forGetter(HubClientSessionResponse::servers)
	).apply(instance, HubClientSessionResponse::new));
}
