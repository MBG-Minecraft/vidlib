package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.latvian.mods.klib.math.KMath;
import net.minecraft.util.Mth;
import org.lwjgl.system.MemoryUtil;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface ImagePreProcessor extends UnaryOperator<NativeImage> {
	ImagePreProcessor NONE = img -> img;

	static NativeImage resize(NativeImage img, int nw, int nh) {
		int ow = img.getWidth();
		int oh = img.getHeight();

		if (nw == ow && nh == oh) {
			return img;
		}

		try (var oimg = img) {
			var dst = new NativeImage(oimg.format(), nw, nh, true);

			for (int y = 0; y < nh; y++) {
				for (int x = 0; x < nw; x++) {
					int ox = x * ow / nw;
					int oy = y * oh / nh;
					dst.setPixel(x, y, oimg.getPixel(ox, oy));
				}
			}

			return dst;
		}
	}

	ImagePreProcessor CLOSEST_POWER_OF_2 = img -> {
		int w = img.getWidth();
		int h = img.getHeight();
		int nw = Mth.smallestEncompassingPowerOfTwo(w);
		int nh = Mth.smallestEncompassingPowerOfTwo(h);
		return resize(img, nw, nh);
	};

	ImagePreProcessor LARGEST_SQUARE = img -> {
		int w = img.getWidth();
		int h = img.getHeight();
		var size = Math.max(w, h);
		return resize(img, size, size);
	};

	ImagePreProcessor SMALLEST_SQUARE = img -> {
		int w = img.getWidth();
		int h = img.getHeight();
		var size = Math.min(w, h);
		return resize(img, size, size);
	};

	ImagePreProcessor CLOSEST_4 = img -> {
		int ow = img.getWidth();
		int oh = img.getHeight();
		var nw = Mth.ceil(ow / 4F) * 4;
		var nh = Mth.ceil(oh / 4F) * 4;
		return resize(img, nw, nh);
	};

	ImagePreProcessor FIT_SQUARE = img -> {
		int w = img.getWidth();
		int h = img.getHeight();

		if (w == h) {
			return img;
		}

		try (var oimg = img) {
			var size = Math.min(w, h);
			var dst = new NativeImage(oimg.format(), size, size, true);

			int sx = (w - size) / 2;
			int sy = (h - size) / 2;

			for (int y = 0; y < size; y++) {
				for (int x = 0; x < size; x++) {
					dst.setPixel(x, y, oimg.getPixel(x + sx, y + sy));
				}
			}

			return dst;
		}
	};

	ImagePreProcessor CIRCLE = img -> {
		int w = img.getWidth();
		int h = img.getHeight();

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				float rx = (x + 0.5F) / (float) w;
				float ry = (y + 0.5F) / (float) h;
				float dSq = KMath.sq(0.5F - rx) + KMath.sq(0.5F - ry);

				if (dSq > 0.25F) {
					img.setPixel(x, y, 0);
				}
			}
		}

		return img;
	};

	static ImagePreProcessor reduce(int cellw, int cellh) {
		return img -> {
			int w = img.getWidth();
			int h = img.getHeight();

			if (w % cellw != 0 || h % cellh != 0) {
				return img;
			}

			int nw = w / cellw;
			int nh = h / cellh;
			var dst = new NativeImage(img.format(), nw, nh, true);

			for (int cy = 0; cy < nh; cy++) {
				for (int cx = 0; cx < nw; cx++) {
					int cell = img.getPixel(cx * cellw, cy * cellh);

					for (int x = 0; x < cellw; x++) {
						for (int y = 0; y < cellh; y++) {
							if (x == 0 && y == 0) {
								continue;
							}

							int c = img.getPixel(cx * cellw + x, cy * cellh + y);

							if (c != cell) {
								dst.close();
								return img;
							}
						}
					}

					dst.setPixel(cx, cy, cell);
				}
			}

			return dst;
		};
	}

	ImagePreProcessor FLIP_Y = img -> {
		long c = img.format().components();
		long w = img.getWidth();
		long cw = w * c;
		long h = img.getHeight();
		long as = img.getPointer();
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

		return img;
	};

	default ImagePreProcessor andThen(ImagePreProcessor after) {
		if (this == NONE) {
			return after;
		} else if (after == NONE) {
			return this;
		} else {
			return img -> after.apply(apply(img));
		}
	}
}
