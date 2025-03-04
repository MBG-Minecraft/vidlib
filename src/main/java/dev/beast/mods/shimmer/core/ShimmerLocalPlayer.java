package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.session.ShimmerClientSessionData;

public interface ShimmerLocalPlayer extends ShimmerClientPlayer, ShimmerClientEntityContainer {
	@Override
	default ShimmerClientSessionData shimmer$sessionData() {
		throw new NoMixinException();
	}
}
