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
import org.lwjgl.system.MemoryUtil;

public class FramebufferUtils {
	public static final int READ_FBO = ((VLDirectStateAccess) ((GlDevice) RenderSystem.getDevice()).directStateAccess()).vl$createFrameBufferObject();

	public static NativeImage capture(RenderTarget framebuffer, int mipLevel) {
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
					img.setPixelABGR(x, y, col);
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

	public static void flipY(NativeImage image) {
		long c = image.format().components();
		long w = image.getWidth();
		long cw = w * c;
		long h = image.getHeight();
		long as = image.getPointer();
		long at = MemoryUtil.nmemAlloc(cw);

		try {
			for (long l = 0L; l < h / 2L; l++) {
				long l1 = l * w * c;
				long l2 = (h - 1L - l) * w * c;
				MemoryUtil.memCopy(as + l1, at, cw);
				MemoryUtil.memCopy(as + l2, as + l1, cw);
				MemoryUtil.memCopy(at, as + l2, cw);
			}
		} finally {
			MemoryUtil.nmemFree(at);
		}
	}
}
