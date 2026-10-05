package dev.mrbeastgaming.mods.hub.api;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.mrbeastgaming.mods.hub.api.data.HubCountry;
import dev.mrbeastgaming.mods.hub.api.data.HubGatewayInfo;
import dev.mrbeastgaming.mods.hub.api.data.HubKeys;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import dev.mrbeastgaming.mods.hub.api.data.HubResponseContext;
import dev.mrbeastgaming.mods.hub.api.data.HubUser;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;
import java.util.UUID;

public record HubServerSessionResponse(
	HubResponseContext ctx,
	UUID id,
	Optional<HubGatewayInfo> gatewayInfo,
	Optional<HubUser> user,
	Optional<HubProject> project,
	HubKeys keys,
	HubKeys sessionKeys,
	byte[] sessionSalt,
	HubCountry country,
	Optional<JsonElement> ops
) {
	public static final Codec<HubServerSessionResponse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubResponseContext.MAP_CODEC.forGetter(HubServerSessionResponse::ctx),
		KLibCodecs.UUID.optionalFieldOf("id", Util.NIL_UUID).forGetter(HubServerSessionResponse::id),
		HubGatewayInfo.CODEC.optionalFieldOf("gateway_info").forGetter(HubServerSessionResponse::gatewayInfo),
		HubUser.CODEC.optionalFieldOf("user").forGetter(HubServerSessionResponse::user),
		HubProject.CODEC.optionalFieldOf("project").forGetter(HubServerSessionResponse::project),
		HubKeys.CODEC.optionalFieldOf("keys", HubKeys.NONE).forGetter(HubServerSessionResponse::keys),
		HubKeys.CODEC.optionalFieldOf("session_keys", HubKeys.NONE).forGetter(HubServerSessionResponse::sessionKeys),
		KLibCodecs.B64_BYTE_ARRAY.optionalFieldOf("session_salt", new byte[0]).forGetter(HubServerSessionResponse::sessionSalt),
		HubCountry.CODEC.optionalFieldOf("country", HubCountry.UNKNOWN).forGetter(HubServerSessionResponse::country),
		ExtraCodecs.JSON.optionalFieldOf("ops").forGetter(HubServerSessionResponse::ops)
	).apply(instance, HubServerSessionResponse::new));
}
