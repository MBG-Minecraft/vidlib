package dev.beast.mods.shimmer.util;

import com.mojang.serialization.Codec;
import com.mojang.util.UndashedUuid;

import java.util.UUID;

public interface ShimmerCodecs {
	Codec<UUID> UUID = Codec.STRING.xmap(UndashedUuid::fromStringLenient, UndashedUuid::toString);
}
