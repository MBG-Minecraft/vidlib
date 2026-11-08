package dev.latvian.mods.vidlib.feature.session;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ClientPlayerSessionData extends PlayerSessionData {
	public static ClientPlayerSessionData of(Player player) {
		return (ClientPlayerSessionData) PlayerSessionData.of(player);
	}

	public ClientPlayerSessionData(ClientSessionData worldData, UUID uuid) {
		super(worldData, uuid);
	}
}
