package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.feature.session.PlayerData;

public class InternalLocalPlayerData extends PlayerData {
	public boolean renderZones;

	InternalLocalPlayerData() {
		super(InternalPlayerData.LOCAL);
		this.renderZones = false;
	}

	InternalLocalPlayerData(
		boolean renderZones
	) {
		super(InternalPlayerData.LOCAL);
		this.renderZones = renderZones;
	}
}
