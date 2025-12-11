package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.entity.ExactEntitySpawnPayload;
import dev.latvian.mods.vidlib.feature.net.Context;
import net.minecraft.world.entity.Entity;

public interface VLClientPlayPacketListener extends VLClientCommonPacketListener {
	default Entity vl$addEntity(Context ctx, ExactEntitySpawnPayload payload) {
		throw new NoMixinException(this);
	}
}
