package dev.latvian.mods.vidlib.core;

import net.minecraft.client.multiplayer.ClientLevel;

public interface VLClientLevel extends VLLevel {
	@Override
	default ClientLevel vl$self() {
		return (ClientLevel) this;
	}
}
