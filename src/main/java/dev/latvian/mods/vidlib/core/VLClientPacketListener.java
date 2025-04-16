package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.entity.ExactEntitySpawnPayload;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.session.LocalClientSessionData;
import net.minecraft.world.entity.Entity;

public interface VLClientPacketListener {
	default LocalClientSessionData vl$sessionData() {
		throw new NoMixinException(this);
	}

	default Entity vl$addEntity(Context ctx, ExactEntitySpawnPayload payload) {
		throw new NoMixinException(this);
	}
}
