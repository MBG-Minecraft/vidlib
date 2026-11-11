package dev.latvian.mods.vidlib.core.api.client;

import net.minecraft.client.player.LocalPlayer;

public interface VLLocalPlayer extends VLClientPlayer {
	@Override
	default LocalPlayer vl$self() {
		return (LocalPlayer) this;
	}
}
