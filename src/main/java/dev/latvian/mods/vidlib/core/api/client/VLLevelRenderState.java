package dev.latvian.mods.vidlib.core.api.client;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.client.VLWorldRenderState;

public interface VLLevelRenderState {
	default VLWorldRenderState vl$state() {
		throw new NoMixinException(this);
	}
}
