package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;

import java.time.Instant;
import java.util.Optional;

public record HubFileAttribute(
	Optional<HubUser> user,
	Instant time,
	HubOrigin origin
) {
	public static final HubFileAttribute DEFAULT = new HubFileAttribute(Optional.empty(), Instant.EPOCH, HubOrigin.UNKNOWN);

	public static final Codec<HubFileAttribute> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubUser.CODEC.optionalFieldOf("user").forGetter(HubFileAttribute::user),
		KLibCodecs.INSTANT.optionalFieldOf("time", Instant.EPOCH).forGetter(HubFileAttribute::time),
		HubOrigin.MAP_CODEC.forGetter(HubFileAttribute::origin)
	).apply(instance, HubFileAttribute::new));
}
