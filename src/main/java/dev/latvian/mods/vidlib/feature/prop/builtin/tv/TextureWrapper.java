package dev.latvian.mods.vidlib.feature.prop.builtin.tv;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.renderer.texture.AbstractTexture;

public class TextureWrapper extends AbstractTexture {
	public TextureWrapper(final int id, final int width, final int height) {
		this.texture = new GlTexture("texturewrapper_" + id, TextureFormat.RGBA8, width, height, 1, id, false) {
			@Override
			public void close() {
			}
		};
		this.texture.setTextureFilter(FilterMode.NEAREST, false);
	}

	@Override
	public void close() {
	}
}
