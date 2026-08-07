package dev.latvian.mods.vidlib.feature.prop.builtin.tv;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.watermedia.api.player.videolan.VideoPlayer;

public class TextureWrapper extends AbstractTexture {
	private final VideoPlayer player;
	private int glId;
	private int width;
	private int height;

	public TextureWrapper(VideoPlayer player) {
		this.player = player;
		this.glId = player.texture();
		this.width = player.width();
		this.height = player.height();

		this.texture = new GlTexture("tv_texture", TextureFormat.RGBA8, width, height, 1, glId, false) {
			@Override
			public void close() {
			}
		};

		this.texture.setTextureFilter(FilterMode.NEAREST, false);
	}

	@Override
	public GpuTexture getTexture() {
		int newGlId = player.texture();
		int newWidth = player.width();
		int newHeight = player.height();

		if (texture == null || glId != newGlId || width != newWidth || height != newHeight) {
			glId = newGlId;
			width = newWidth;
			height = newHeight;

			texture = new GlTexture("tv_texture", TextureFormat.RGBA8, width, height, 1, glId, false) {
				@Override
				public void close() {
				}
			};

			texture.setTextureFilter(FilterMode.NEAREST, false);
		}

		return texture;
	}

	@Override
	public void close() {
	}
}
