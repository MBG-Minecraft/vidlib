package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.feature.visual.DynamicSpriteTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface VLTextureAtlasSprite {
	default void vl$invalidateDynamicSpriteTexture() {
	}

	default DynamicSpriteTexture vl$getDynamicSpriteTexture(Minecraft mc) {
		throw new NoMixinException(this);
	}

	default UV uv() {
		var tex = (TextureAtlasSprite) this;
		return new UV(tex.getU0(), tex.getV0(), tex.getU1(), tex.getV1());
	}
}
