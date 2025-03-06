package dev.beast.mods.shimmer.feature.session;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShimmerRemoteClientSessionData extends ShimmerClientSessionData {
	public final Set<String> tags;

	public ShimmerRemoteClientSessionData(UUID uuid) {
		super(uuid);
		this.tags = new HashSet<>(0);
	}
}
