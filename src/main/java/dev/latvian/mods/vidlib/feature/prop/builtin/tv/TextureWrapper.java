package dev.latvian.mods.vidlib.feature.prop.builtin.tv;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.watermedia.api.player.videolan.VideoPlayer;

public class TextureWrapper extends AbstractTexture {
	private static final int GL_FRAMEBUFFER = 0x8D40;
	private static final int GL_READ_FRAMEBUFFER = 0x8CA8;
	private static final int GL_DRAW_FRAMEBUFFER = 0x8CA9;
	private static final int GL_COLOR_ATTACHMENT0 = 0x8CE0;
	private static final int GL_TEXTURE_2D = 0x0DE1;
	private static final int GL_COLOR_BUFFER_BIT = 0x00004000;
	private static final int GL_NEAREST = 0x2600;

	private final VideoPlayer player;
	private int glId;
	private int width;
	private int height;
	private int readFbo;
	private int drawFbo;

	public TextureWrapper(VideoPlayer player) {
		this.player = player;
		this.glId = player.texture();
		this.width = player.width();
		this.height = player.height();

		createTexture();
		installSampler();
	}

	private void createTexture() {
		this.texture = RenderSystem.getDevice()
			.createTexture("tv_texture", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING, GpuFormat.RGBA8_UNORM, width, height, 1, 1);
		this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
	}

	private void installSampler() {
		this.sampler = RenderSystem.getSamplerCache().getSampler(
			com.mojang.renderpearl.api.textures.AddressMode.CLAMP_TO_EDGE,
			com.mojang.renderpearl.api.textures.AddressMode.CLAMP_TO_EDGE,
			FilterMode.NEAREST,
			FilterMode.NEAREST,
			false
		);
	}

	private void releaseFramebuffers() {
		if (readFbo != 0) {
			GlStateManager._glDeleteFramebuffers(readFbo);
			readFbo = 0;
		}

		if (drawFbo != 0) {
			GlStateManager._glDeleteFramebuffers(drawFbo);
			drawFbo = 0;
		}
	}

	/**
	 * 26.3 has no way to adopt a foreign GL texture as a {@link GpuTexture} (the old
	 * {@code GlTexture}-with-id constructor is package private), so the video frame is blitted
	 * into a GPU texture we own every frame.
	 */
	private void blit() {
		if (texture == null || readFbo == 0 || drawFbo == 0) {
			return;
		}

		GlStateManager._glBindFramebuffer(GL_READ_FRAMEBUFFER, readFbo);
		GlStateManager._glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, glId, 0);
		GlStateManager._glBindFramebuffer(GL_DRAW_FRAMEBUFFER, drawFbo);
		GlStateManager._glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, ((com.mojang.renderpearl.backend.opengl.GlTexture) texture).glId(), 0);
		GlStateManager._glBlitFrameBuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST);
		GlStateManager._glBindFramebuffer(GL_FRAMEBUFFER, 0);
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

			releaseFramebuffers();

			if (texture != null) {
				texture.close();
				texture = null;
			}

			createTexture();
			installSampler();
		}

		if (readFbo == 0) {
			readFbo = GlStateManager.glGenFramebuffers();
		}

		if (drawFbo == 0) {
			drawFbo = GlStateManager.glGenFramebuffers();
		}

		blit();
		return texture;
	}

	@Override
	public void close() {
		releaseFramebuffers();
		super.close();
	}
}
