package dev.beast.mods.shimmer.feature.skybox;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import dev.beast.mods.shimmer.Shimmer;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.IntUnaryOperator;

public class SkyboxTexture extends ReloadableTexture implements Dumpable {
	public final Skybox skybox;
	private int resolution;

	public SkyboxTexture(Skybox skybox, ResourceLocation id) {
		super(id);
		this.skybox = skybox;
	}

	@Override
	public void close() {
		super.close();
		skybox.skyboxTexture = null;
	}

	@Override
	public TextureContents loadContents(ResourceManager manager) throws IOException {
		try (var in = manager.getResource(skybox.texture).orElseThrow().open()) {
			var src = NativeImage.read(in);
			var image = process(skybox.texture, src, src.getWidth(), src.getHeight());
			resolution = image.getHeight() / 2;
			return new TextureContents(image, new TextureMetadataSection(false, false));
		}
	}

	@SuppressWarnings({"ConstantValue", "PointlessArithmeticExpression"})
	private static NativeImage process(ResourceLocation location, NativeImage src, int srcW, int srcH) {
		if (srcW == srcH * 2) { // Pre-mapped 4x2
			return src;
		} else if (srcW == srcH || srcW * 3 == srcH * 4) { // Cube Map 4x4 or 4x3
			int s = srcW / 4;
			var img = new NativeImage(s * 4, s * 2, false);

			for (int y = 0; y < s; y++) {
				for (int x = 0; x < s; x++) {
					img.setPixel(x + s * 0, y + s * 0, 0xFF000000);
					img.setPixel(x + s * 1, y + s * 0, src.getPixel(x + s * 1, y + s * 0));
					img.setPixel(x + s * 2, y + s * 0, src.getPixel(x + s * 1, y + s * 2));
					img.setPixel(x + s * 3, y + s * 0, 0xFF000000);
					img.setPixel(x + s * 0, y + s * 1, src.getPixel(x + s * 0, y + s * 1));
					img.setPixel(x + s * 1, y + s * 1, src.getPixel(x + s * 1, y + s * 1));
					img.setPixel(x + s * 2, y + s * 1, src.getPixel(x + s * 2, y + s * 1));
					img.setPixel(x + s * 3, y + s * 1, src.getPixel(x + s * 3, y + s * 1));
				}
			}

			return img;
		} else if (srcH > srcW) { // Vertical Gradient
			var img = new NativeImage(srcH * 4, srcH * 2, false);

			var g = new int[src.getHeight()];

			for (var y = 0; y < src.getHeight(); y++) {
				g[y] = src.getPixel(0, y);
			}

			var s = g.length;

			for (int y = 0; y < s; y++) {
				for (int x = 0; x < s; x++) {
					img.setPixel(x + s * 0, y + s * 0, 0xFF000000);
					img.setPixel(x + s * 3, y + s * 0, 0xFF000000);

					int nx = x - s / 2;
					int ny = y - s / 2;

					for (int face = 0; face < 3; face++) {
						int nz;

						switch (face) {
							case 0:
								nx = -nx;
								nz = -s / 2;
								break;
							case 1:
								nz = s / 2;
								break;
							default:
								nz = nx;
								nx = -s / 2;
								break;
						}

						int colorIndex = Math.min((int) (Math.acos(nz / Math.sqrt(nx * nx + ny * ny + nz * nz)) / Math.PI * (s - 1)), s - 1);
						int color = g[colorIndex];

						if (face == 0) {
							img.setPixel(x + s * 2, y + s * 0, color);
						} else if (face == 1) {
							img.setPixel(x + s * 1, y + s * 0, color);
						} else if (face == 2) {
							img.setPixel(y + s * 0, x + s * 1, color);
							img.setPixel(y + s * 1, x + s * 1, color);
							img.setPixel(y + s * 2, x + s * 1, color);
							img.setPixel(y + s * 3, x + s * 1, color);
						}
					}
				}
			}

			return img;
		}

		Shimmer.LOGGER.error("Invalid source skybox texture from " + location + " Must be either foldable 4x4, 4x3 or a vertical gradient");

		var img = new NativeImage(16, 16, false);
		img.fillRect(0, 0, 16, 16, 0xFF000000);
		return img;
	}

	@Override
	public void dumpContents(ResourceLocation id, Path path) {
		TextureUtil.writeAsPNG(path, id.toDebugFileName(), getTexture(), 0, IntUnaryOperator.identity());
	}
}
