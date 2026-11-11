package dev.latvian.mods.vidlib.core.api.client;

import dev.latvian.mods.vidlib.feature.visual.DynamicSpriteTexture;
import org.jetbrains.annotations.Nullable;

public interface VLSpriteContents {
	default void vl$setDynamicSpriteTexture(@Nullable DynamicSpriteTexture texture) {
	}
}
