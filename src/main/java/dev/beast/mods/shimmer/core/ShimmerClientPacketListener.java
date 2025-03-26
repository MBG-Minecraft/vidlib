package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.entity.ExactEntitySpawnPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import dev.beast.mods.shimmer.feature.session.ShimmerLocalClientSessionData;

public interface ShimmerClientPacketListener {
	default ShimmerLocalClientSessionData shimmer$sessionData() {
		throw new NoMixinException(this);
	}

	default void shimmer$addEntity(ShimmerPayloadContext ctx, ExactEntitySpawnPayload payload) {
		throw new NoMixinException(this);
	}
}
