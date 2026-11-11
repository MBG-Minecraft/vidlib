package dev.latvian.mods.vidlib.core.api.client;

import dev.latvian.mods.vidlib.core.api.common.VLPlayer;
import net.minecraft.client.player.AbstractClientPlayer;

public interface VLClientPlayer extends VLPlayer {
	@Override
	default AbstractClientPlayer vl$self() {
		return (AbstractClientPlayer) this;
	}
}
