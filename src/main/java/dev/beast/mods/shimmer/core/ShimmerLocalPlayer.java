package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.session.ShimmerLocalClientSessionData;

public interface ShimmerLocalPlayer extends ShimmerClientPlayer, ShimmerClientEntityContainer {
	@Override
	default ShimmerLocalClientSessionData shimmer$sessionData() {
		throw new NoMixinException();
	}
}
