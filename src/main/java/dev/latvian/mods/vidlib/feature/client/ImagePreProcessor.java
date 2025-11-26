package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.latvian.mods.klib.math.KMath;
import net.minecraft.util.Mth;

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

	default ImagePreProcessor andThen(ImagePreProcessor after) {
		return img -> after.apply(apply(img));
	}
}
