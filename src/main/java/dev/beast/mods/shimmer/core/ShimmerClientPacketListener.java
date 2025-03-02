package dev.beast.mods.shimmer.core;

public interface ShimmerClientPacketListener {
	default ShimmerClientSessionData shimmer$sessionData() {
		throw new NoMixinException();
	}
}
