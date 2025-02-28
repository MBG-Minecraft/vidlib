package dev.beast.mods.shimmer.util;

import com.mojang.serialization.Codec;
import com.mojang.util.UndashedUuid;
import dev.beast.mods.shimmer.Shimmer;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public interface ShimmerCodecs {
	Codec<UUID> UUID = Codec.STRING.xmap(UndashedUuid::fromStringLenient, UndashedUuid::toString);

	Codec<ResourceLocation> SHIMMER_ID = Codec.STRING.xmap(s -> s.indexOf(':') == -1 ? Shimmer.id(s) : ResourceLocation.parse(s), rl -> rl.getNamespace().equals(Shimmer.ID) ? rl.getPath() : rl.toString());
}
