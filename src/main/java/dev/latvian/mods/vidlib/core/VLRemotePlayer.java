package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.session.RemoteClientPlayerSessionData;
import net.minecraft.client.player.RemotePlayer;

public interface VLRemotePlayer extends VLClientPlayer {
	@Override
	default RemotePlayer vl$self() {
		return (RemotePlayer) this;
	}

	@Override
	default RemoteClientPlayerSessionData vl$sessionData() {
		throw new NoMixinException(this);
	}
}
