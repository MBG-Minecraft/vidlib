package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.util.Empty;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public interface VLRenderType {
	RenderSetup vl$getState();

	@Nullable
	default Identifier vl$getTexture() {
		var textures = ((VLRenderSetup) (Object) vl$getState()).vl$getTextures();

		for (var binding : textures.values()) {
			return ((VLRenderSetupTextureBinding) binding).vl$getLocation();
		}

		return null;
	}

	default Identifier vl$getTextureSafe() {
		var tex = vl$getTexture();
		return tex == null ? Empty.TEXTURE : tex;
	}
}
