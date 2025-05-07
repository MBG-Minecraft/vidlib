package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import net.minecraft.client.player.AbstractClientPlayer;

public interface VLClientPlayer extends VLPlayer {
	@Override
	default AbstractClientPlayer vl$self() {
		return (AbstractClientPlayer) this;
	}

	@Override
	default ClientSessionData vl$sessionData() {
		throw new NoMixinException(this);
	}
}
