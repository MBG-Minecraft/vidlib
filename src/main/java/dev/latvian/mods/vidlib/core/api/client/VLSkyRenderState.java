package dev.latvian.mods.vidlib.core.api.client;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.atmosphere.VLExtraSkyRenderState;

public interface VLSkyRenderState {
	default VLExtraSkyRenderState vl$state() {
		throw new NoMixinException(this);
	}
}
