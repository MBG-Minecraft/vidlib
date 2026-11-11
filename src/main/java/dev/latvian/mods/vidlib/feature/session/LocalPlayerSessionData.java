package dev.latvian.mods.vidlib.feature.session;

import java.util.UUID;

public class LocalPlayerSessionData extends ClientPlayerSessionData {
	public LocalPlayerSessionData(ClientSessionData globalData, UUID uuid) {
		super(globalData, uuid);
	}
}
