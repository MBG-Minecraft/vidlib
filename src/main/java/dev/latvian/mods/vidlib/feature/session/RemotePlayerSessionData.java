package dev.latvian.mods.vidlib.feature.session;

import java.util.UUID;

public class RemotePlayerSessionData extends ClientPlayerSessionData {
	public RemotePlayerSessionData(ClientSessionData globalData, UUID uuid) {
		super(globalData, uuid);
	}
}
