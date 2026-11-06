package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.util.LevelGameTimeProvider;

import java.util.UUID;

public class ClientPlayerSessionData extends PlayerSessionData {
	public ClientPlayerSessionData(ClientWorldSessionData worldData, UUID uuid) {
		super(worldData, uuid);
	}
}
