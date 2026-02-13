package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.vidlib.core.VLGameTimeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;

import java.util.UUID;

public class RemoteClientSessionData extends ClientSessionData {
	public RemoteClientSessionData(UUID uuid, VLGameTimeProvider timeProvider) {
		super(uuid, timeProvider);
	}

	public void preTick(Minecraft mc, ClientLevel level, RemotePlayer player) {
		updateOverrides(player);
	}
}
