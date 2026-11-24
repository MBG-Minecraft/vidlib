package dev.latvian.mods.vidlib.core.api.client;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.session.ClientLevelSessionData;
import net.minecraft.client.multiplayer.ClientLevel;

public interface VLClientLevel {
	default ClientLevel vl$self() {
		return (ClientLevel) this;
	}

	default ClientLevelSessionData vl$getLevelData() {
		throw new NoMixinException(this);
	}

	default void vl$updateAttributes() {
	}
}
