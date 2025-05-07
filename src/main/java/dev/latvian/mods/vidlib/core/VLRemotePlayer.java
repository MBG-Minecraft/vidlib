package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.session.RemoteClientSessionData;
import net.minecraft.client.player.RemotePlayer;

public interface VLRemotePlayer extends VLClientPlayer {
	@Override
	default RemotePlayer vl$self() {
		return (RemotePlayer) this;
	}

	@Override
	default RemoteClientSessionData vl$sessionData() {
		throw new NoMixinException(this);
	}
}
