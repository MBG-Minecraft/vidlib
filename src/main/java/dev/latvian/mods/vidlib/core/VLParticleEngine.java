package dev.latvian.mods.vidlib.core;

import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface VLParticleEngine {
	default TextureAtlas getTextureAtlas() {
		throw new NoMixinException(this);
	}

	@Nullable
	default SpriteSet getSpriteSet(ResourceLocation id) {
		return null;
	}
}
