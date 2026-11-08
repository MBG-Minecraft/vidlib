package dev.latvian.mods.vidlib.feature.session;

import java.util.UUID;

public class LocalPlayerSessionData extends PlayerSessionData {
	public LocalPlayerSessionData(GlobalSessionData worldData, UUID uuid) {
		super(worldData, uuid);
	}
}
