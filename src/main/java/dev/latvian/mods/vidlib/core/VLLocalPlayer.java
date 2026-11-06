package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.session.LocalClientPlayerSessionData;
import net.minecraft.client.player.LocalPlayer;

public interface VLLocalPlayer extends VLClientPlayer {
	@Override
	default LocalPlayer vl$self() {
		return (LocalPlayer) this;
	}

	@Override
	default LocalClientPlayerSessionData vl$sessionData() {
		throw new NoMixinException(this);
	}
}
