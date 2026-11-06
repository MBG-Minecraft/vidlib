package dev.latvian.mods.vidlib.feature.session;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;

import java.util.UUID;

public class RemoteClientPlayerSessionData extends ClientPlayerSessionData {
	public RemoteClientPlayerSessionData(ClientWorldSessionData worldData, UUID uuid) {
		super(worldData, uuid);
	}

	public void preTick(Minecraft mc, ClientLevel level, RemotePlayer player) {
		updateOverrides(player);
	}
}
