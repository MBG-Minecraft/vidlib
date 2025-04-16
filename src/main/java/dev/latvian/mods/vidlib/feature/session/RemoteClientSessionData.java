package dev.latvian.mods.vidlib.feature.session;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;

import java.util.UUID;

public class RemoteClientSessionData extends ClientSessionData {
	public RemoteClientSessionData(UUID uuid) {
		super(uuid);
	}

	public void preTick(Minecraft mc, ClientLevel level, RemotePlayer player) {
		updateOverrides(player);
	}
}
