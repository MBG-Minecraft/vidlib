package dev.latvian.mods.vidlib.core.api.client;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.atmosphere.SkyboxData;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

public interface VLSkyRenderState {
	default void vl$set(boolean sun, boolean moon, @Nullable Quaternionfc celestialTilt, @Nullable SkyboxData skyboxData) {
		throw new NoMixinException(this);
	}

	default boolean vl$sun() {
		throw new NoMixinException(this);
	}

	default boolean vl$moon() {
		throw new NoMixinException(this);
	}

	@Nullable
	default Quaternionfc vl$celestialTilt() {
		throw new NoMixinException(this);
	}

	@Nullable
	default SkyboxData vl$skyboxTextureData() {
		throw new NoMixinException(this);
	}
}
