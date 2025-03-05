package dev.beast.mods.shimmer.feature.session;

import java.util.List;
import java.util.UUID;

public class ShimmerClientSessionData extends ShimmerSessionData {
	public ShimmerClientSessionData(UUID uuid) {
		super(uuid);
	}

	protected void updateSessionData(List<PlayerData> update) {
		for (var data : update) {
			init(data.type()).playerData = data;
		}
	}
}
