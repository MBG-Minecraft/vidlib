package dev.mrbeastgaming.mods.hub.api;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.Hex32;
import dev.mrbeastgaming.mods.hub.api.data.HubDBObject;
import dev.mrbeastgaming.mods.hub.api.data.HubDisplayContext;
import dev.mrbeastgaming.mods.hub.api.data.HubKeys;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public record HubServerSessionResponse(
	HubDisplayContext ctx,
	UUID id,
	Optional<URI> gateway,
	Optional<String> gatewayToken,
	Hex32 user,
	Hex32 project,
	Optional<HubKeys> keys,
	Optional<HubKeys> sessionKeys,
	byte[] sessionSalt,
	Optional<JsonElement> ops
) {
	public static final Codec<HubServerSessionResponse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubDisplayContext.MAP_CODEC.forGetter(HubServerSessionResponse::ctx),
		KLibCodecs.UUID.optionalFieldOf("id", Util.NIL_UUID).forGetter(HubServerSessionResponse::id),
		HubAPI.WS_URI_BASE_CODEC.optionalFieldOf("gateway").forGetter(HubServerSessionResponse::gateway),
		Codec.STRING.optionalFieldOf("gateway_token").forGetter(HubServerSessionResponse::gatewayToken),
		HubDBObject.idCodec("user").forGetter(HubServerSessionResponse::user),
		HubDBObject.idCodec("project").forGetter(HubServerSessionResponse::project),
		HubKeys.CODEC.optionalFieldOf("keys").forGetter(HubServerSessionResponse::keys),
		HubKeys.CODEC.optionalFieldOf("session_keys").forGetter(HubServerSessionResponse::sessionKeys),
		KLibCodecs.B64_BYTE_ARRAY.optionalFieldOf("session_salt", new byte[0]).forGetter(HubServerSessionResponse::sessionSalt),
		ExtraCodecs.JSON.optionalFieldOf("ops").forGetter(HubServerSessionResponse::ops)
	).apply(instance, HubServerSessionResponse::new));
}
