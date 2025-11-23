package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.visual.DynamicSpriteTexture;
import net.minecraft.client.Minecraft;

public interface VLTextureAtlasSprite {
	default void vl$invalidateDynamicSpriteTexture() {
	}

	default DynamicSpriteTexture vl$getDynamicSpriteTexture(Minecraft mc) {
		throw new NoMixinException(this);
	}
}
