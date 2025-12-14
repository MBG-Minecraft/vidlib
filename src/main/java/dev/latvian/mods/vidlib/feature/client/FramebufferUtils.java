package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.latvian.mods.vidlib.core.VLDirectStateAccess;

public class FramebufferUtils {
	public static final int READ_FBO = ((VLDirectStateAccess) ((GlDevice) RenderSystem.getDevice()).directStateAccess()).vl$createFrameBufferObject();

	public static NativeImage capture(RenderTarget framebuffer) {
		return capture(framebuffer, 0, false, false);
	}

	public static NativeImage capture(RenderTarget framebuffer, int mipLevel, boolean flipX, boolean flipY) {
		var colorTexture = framebuffer.getColorTexture();

		if (colorTexture == null) {
			throw new IllegalStateException("Tried to capture screenshot of an incomplete framebuffer");
		}

		var device = (GlDevice) RenderSystem.getDevice();
		int w = framebuffer.width;
		int h = framebuffer.height;

		int mipWidth = colorTexture.getWidth(mipLevel);
		int mipHeight = colorTexture.getHeight(mipLevel);

		var buffer = device.createBuffer(() -> "Screenshot buffer", BufferType.PIXEL_PACK, BufferUsage.STATIC_READ, w * h * colorTexture.getFormat().pixelSize());

		GlStateManager.clearGlErrors();
		device.directStateAccess().bindFrameBufferTextures(READ_FBO, ((GlTexture) colorTexture).glId(), 0, mipLevel, 36008);
		GlStateManager._glBindBuffer(GlConst.toGl(buffer.type()), buffer.vl$getHandle());
		GlStateManager._pixelStore(3330, mipWidth);
		GlStateManager._readPixels(
			0,
			0,
			mipWidth,
			mipHeight,
			GlConst.toGlExternalId(colorTexture.getFormat()),
			GlConst.toGlType(colorTexture.getFormat()),
			0
		);

		var img = new NativeImage(w, h, false);

		try (var view = device.createCommandEncoder().readBuffer(buffer)) {
			var buf = view.data();

			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int col = buf.getInt((x + y * w) * colorTexture.getFormat().pixelSize());
					img.setPixelABGR(flipX ? (w - x - 1) : x, flipY ? (h - y - 1) : y, col);
				}
			}
		}

		GlStateManager._glFramebufferTexture2D(36008, 36064, 3553, 0, mipLevel);
		GlStateManager._glBindFramebuffer(36008, 0);
		GlStateManager._glBindBuffer(GlConst.toGl(buffer.type()), 0);
		buffer.close();

		int error = GlStateManager._getError();

		if (error != 0) {
			throw new IllegalStateException("Couldn't perform copyTobuffer for texture " + colorTexture.getLabel() + ": GL error " + error);
		}

		return img;
	}
}
