package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.util.Empty;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface VLRenderType {
	@Nullable
	default ResourceLocation vl$getTexture() {
		return this instanceof RenderType.CompositeRenderType t ? t.state.textureState instanceof RenderStateShard.TextureStateShard s ? s.texture.orElse(null) : null : null;
	}

	default ResourceLocation vl$getTextureSafe() {
		var tex = vl$getTexture();
		return tex == null ? Empty.TEXTURE : tex;
	}
}
