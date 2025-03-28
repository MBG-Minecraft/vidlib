package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.entity.ExactEntitySpawnPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import dev.beast.mods.shimmer.feature.session.ShimmerLocalClientSessionData;
import net.minecraft.world.entity.Entity;

public interface ShimmerClientPacketListener {
	default ShimmerLocalClientSessionData shimmer$sessionData() {
		throw new NoMixinException(this);
	}

	default Entity shimmer$addEntity(ShimmerPayloadContext ctx, ExactEntitySpawnPayload payload) {
		throw new NoMixinException(this);
	}
}
