package dev.latvian.mods.vidlib.math.kvector;

import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import net.minecraft.network.RegistryFriendlyByteBuf;

public interface VLKVectors {
	static void register(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, KVector> registry) {
		registry.register(FollowingPropKVector.TYPE);
	}
}
