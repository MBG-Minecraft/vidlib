package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.platform.NativeImage;

public class BuiltInImages {
	private static NativeImage loadingImage = null;

	public static synchronized NativeImage loading() {
		var img = loadingImage;

		if (img == null || img.getPointer() == 0L) {
			img = new NativeImage(16, 16, true);
			img.fillRect(3, 7, 2, 2, 0xFFFFFFFF);
			img.fillRect(7, 7, 2, 2, 0xFFFFFFFF);
			img.fillRect(11, 7, 2, 2, 0xFFFFFFFF);
			loadingImage = img;
		}

		return img;
	}
}
