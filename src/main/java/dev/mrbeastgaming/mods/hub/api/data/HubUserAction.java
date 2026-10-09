package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;

import java.time.Instant;
import java.util.Optional;

public record HubUserAction(
	Optional<HubUser> user,
	Instant time,
	HubOrigin origin
) {
	public static final HubUserAction UNKNOWN = new HubUserAction(Optional.empty(), Instant.EPOCH, HubOrigin.UNKNOWN);

	public static final Codec<HubUserAction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubUser.CODEC.optionalFieldOf("user").forGetter(HubUserAction::user),
		KLibCodecs.INSTANT.optionalFieldOf("time", Instant.EPOCH).forGetter(HubUserAction::time),
		HubOrigin.MAP_CODEC.forGetter(HubUserAction::origin)
	).apply(instance, HubUserAction::new));
}
