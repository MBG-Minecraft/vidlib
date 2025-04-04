package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.session.ShimmerServerSessionData;

public interface ShimmerServerPacketListener {
	default ShimmerServerSessionData shimmer$sessionData() {
		throw new NoMixinException(this);
	}

	default void shimmer$sessionData(ShimmerServerSessionData data) {
		throw new NoMixinException(this);
	}
}
