package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.Hex32;

public record HubOrigin(Hex32 ip, String country) {
	public static final MapCodec<HubOrigin> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Hex32.LENIENT_CODEC.optionalFieldOf("ip", Hex32.NONE).forGetter(HubOrigin::ip),
		Codec.STRING.optionalFieldOf("country", "").forGetter(HubOrigin::country)
	).apply(i, HubOrigin::new));
}
