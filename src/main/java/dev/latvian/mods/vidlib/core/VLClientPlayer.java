package dev.latvian.mods.vidlib.core;

import net.minecraft.client.player.AbstractClientPlayer;

public interface VLClientPlayer extends VLPlayer {
	@Override
	default AbstractClientPlayer vl$self() {
		return (AbstractClientPlayer) this;
	}
}
