package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public interface ScreenEffectInstance {
	ScreenEffectShaderType shaderType();

	default void snap() {
	}

	default void update(KNumberContext ctx) {
	}

	void upload(IntArrayList arr, float delta);
}
