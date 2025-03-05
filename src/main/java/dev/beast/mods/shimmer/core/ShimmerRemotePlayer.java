package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.session.ShimmerRemoteClientSessionData;

public interface ShimmerRemotePlayer extends ShimmerClientPlayer {
	@Override
	default ShimmerRemoteClientSessionData shimmer$sessionData() {
		throw new NoMixinException();
	}
}
