package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.session.ClientPlayerSessionData;
import net.minecraft.client.player.AbstractClientPlayer;

public interface VLClientPlayer extends VLPlayer {
	@Override
	default AbstractClientPlayer vl$self() {
		return (AbstractClientPlayer) this;
	}

	@Override
	default ClientPlayerSessionData vl$sessionData() {
		throw new NoMixinException(this);
	}
}
