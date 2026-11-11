package dev.latvian.mods.vidlib.core.api.client;

import dev.latvian.mods.vidlib.core.api.common.VLLevel;
import net.minecraft.client.multiplayer.ClientLevel;

public interface VLClientLevel extends VLLevel {
	@Override
	default ClientLevel vl$self() {
		return (ClientLevel) this;
	}
}
