package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.util.LevelGameTimeProvider;

import java.util.UUID;

public class ClientSessionData extends SessionData {
	public ClientSessionData(UUID uuid, LevelGameTimeProvider timeProvider) {
		super(uuid, timeProvider);
	}
}
