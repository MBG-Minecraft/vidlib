package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;
import dev.mrbeastgaming.mods.hub.api.data.HubCountry;
import dev.mrbeastgaming.mods.hub.api.data.HubGameServer;
import dev.mrbeastgaming.mods.hub.api.data.HubGatewayInfo;
import dev.mrbeastgaming.mods.hub.api.data.HubKeys;
import dev.mrbeastgaming.mods.hub.api.data.HubParticipant;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import dev.mrbeastgaming.mods.hub.api.data.HubResponseContext;
import dev.mrbeastgaming.mods.hub.api.data.HubUser;
import dev.mrbeastgaming.mods.hub.api.data.HubUserCapabilities;
import net.minecraft.Util;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record HubClientSessionResponse(
	HubResponseContext ctx,
	UUID id,
	Optional<HubGatewayInfo> gatewayInfo,
	Optional<HubUser> user,
	Optional<HubProject> project,
	Optional<HubParticipant> participant,
	HubUserCapabilities capabilities,
	HubKeys keys,
	HubKeys sessionKeys,
	byte[] sessionSalt,
	HubCountry country,
	List<HubGameServer> servers
) {
	public static final Codec<HubClientSessionResponse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubResponseContext.MAP_CODEC.forGetter(HubClientSessionResponse::ctx),
		KLibCodecs.UUID.optionalFieldOf("id", Util.NIL_UUID).forGetter(HubClientSessionResponse::id),
		HubGatewayInfo.CODEC.optionalFieldOf("gateway_info").forGetter(HubClientSessionResponse::gatewayInfo),
		HubUser.CODEC.optionalFieldOf("user").forGetter(HubClientSessionResponse::user),
		HubProject.CODEC.optionalFieldOf("project").forGetter(HubClientSessionResponse::project),
		HubParticipant.CODEC.optionalFieldOf("participant").forGetter(HubClientSessionResponse::participant),
		HubUserCapabilities.CODEC.optionalFieldOf("capabilities", HubUserCapabilities.DEFAULT).forGetter(HubClientSessionResponse::capabilities),
		HubKeys.CODEC.optionalFieldOf("keys", HubKeys.NONE).forGetter(HubClientSessionResponse::keys),
		HubKeys.CODEC.optionalFieldOf("session_keys", HubKeys.NONE).forGetter(HubClientSessionResponse::sessionKeys),
		KLibCodecs.B64_BYTE_ARRAY.optionalFieldOf("session_salt", new byte[0]).forGetter(HubClientSessionResponse::sessionSalt),
		HubCountry.CODEC.optionalFieldOf("country", HubCountry.UNKNOWN).forGetter(HubClientSessionResponse::country),
		HubGameServer.LIST_CODEC.optionalFieldOf("servers", List.of()).forGetter(HubClientSessionResponse::servers)
	).apply(instance, HubClientSessionResponse::new));
}
