package dev.latvian.mods.vidlib.core.api.client;

import net.minecraft.client.player.RemotePlayer;

public interface VLRemotePlayer extends VLClientPlayer {
	@Override
	default RemotePlayer vl$self() {
		return (RemotePlayer) this;
	}
}
