package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.session.ShimmerLocalClientSessionData;

public interface ShimmerClientPacketListener {
	default ShimmerLocalClientSessionData shimmer$sessionData() {
		throw new NoMixinException();
	}
}
