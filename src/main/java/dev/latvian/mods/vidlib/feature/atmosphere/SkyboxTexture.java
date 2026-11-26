package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.imgui.icon.DirectImageImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.IntUnaryOperator;

public class SkyboxTexture extends ReloadableTexture implements Dumpable {
	public final SkyboxTextureId id;
	public final boolean equirectangular;
	private ImIcon icon;

	public SkyboxTexture(SkyboxTextureId id, boolean equirectangular) {
		super(id.generated().texturePath());
		this.id = id;
		this.equirectangular = equirectangular;
	}

	@Override
	public void close() {
		super.close();
		icon = null;
	}

	public ImIcon getIcon() {
		if (icon == null) {
			icon = new DirectImageImIcon(getTexture(), 0F, 0.5F, 0.25F, 1F);
		}

		return icon;
	}

	@Override
	public TextureContents loadContents(ResourceManager manager) throws IOException {
		try (var in = manager.getResource(id.source().texturePath()).orElseThrow().open()) {
			var src = NativeImage.read(in);
			var image = equirectangular ? src : process(id.source().id().toString(), src, src.getWidth(), src.getHeight());
			return new TextureContents(image, new TextureMetadataSection(false, false, MipmapStrategy.AUTO, 0F));
		}
	}

	@SuppressWarnings({"ConstantValue", "PointlessArithmeticExpression"})
	public static NativeImage process(String name, NativeImage src, int srcW, int srcH) {
		if (srcW * 2 == srcH * 3) { // Cube Map 3x2
			// EWU
			// DNS
			return src;
		} else if (srcW == srcH * 2) { // Cube Map 4x2
			// -UD-
			// WNES

			int s = srcW / 4;
			var img = new NativeImage(s * 3, s * 2, false);
			src.copyRect(img, s * 2, s * 0, s * 0, s, s, s, false, false); // D
			src.copyRect(img, s * 1, s * 0, s * 2, 0, s, s, false, false); // U
			src.copyRect(img, s * 1, s * 1, s * 1, s, s, s, false, false); // N
			src.copyRect(img, s * 3, s * 1, s * 2, s, s, s, false, false); // S
			src.copyRect(img, s * 0, s * 1, s * 1, 0, s, s, false, false); // W
			src.copyRect(img, s * 2, s * 1, s * 0, 0, s, s, false, false); // E
			src.close();
			return img;
		} else if (srcW == srcH || srcW * 3 == srcH * 4) { // Cube Map 4x4 or 4x3
			// -U--
			// WNES
			// -D--

			int s = srcW / 4;
			var img = new NativeImage(s * 3, s * 2, false);
			src.copyRect(img, s * 1, s * 2, s * 0, s, s, s, false, false); // D
			src.copyRect(img, s * 1, s * 0, s * 2, 0, s, s, false, false); // U
			src.copyRect(img, s * 1, s * 1, s * 1, s, s, s, false, false); // N
			src.copyRect(img, s * 3, s * 1, s * 2, s, s, s, false, false); // S
			src.copyRect(img, s * 0, s * 1, s * 1, 0, s, s, false, false); // W
			src.copyRect(img, s * 2, s * 1, s * 0, 0, s, s, false, false); // E
			src.close();
			return img;
		} else if (srcH > srcW) { // Vertical Gradient
			var c4x2 = new NativeImage(srcH * 4, srcH * 2, false);

			var g = new int[srcH];

			for (var y = 0; y < srcH; y++) {
				g[y] = src.getPixel(0, y);
			}

			src.close();

			var s = g.length;

			for (int y = 0; y < s; y++) {
				for (int x = 0; x < s; x++) {
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
							c4x2.setPixel(x + s * 2, y + s * 0, color);
						} else if (face == 1) {
							c4x2.setPixel(x + s * 1, y + s * 0, color);
						} else if (face == 2) {
							c4x2.setPixel(y + s * 0, x + s * 1, color);
							c4x2.setPixel(y + s * 1, x + s * 1, color);
							c4x2.setPixel(y + s * 2, x + s * 1, color);
							c4x2.setPixel(y + s * 3, x + s * 1, color);
						}
					}
				}
			}

			var img = new NativeImage(s * 3, s * 2, false);
			c4x2.copyRect(img, s * 1, s * 2, s * 0, s, s, s, false, false); // D
			c4x2.copyRect(img, s * 1, s * 0, s * 2, 0, s, s, false, false); // U
			c4x2.copyRect(img, s * 1, s * 1, s * 1, s, s, s, false, false); // N
			c4x2.copyRect(img, s * 3, s * 1, s * 2, s, s, s, false, false); // S
			c4x2.copyRect(img, s * 0, s * 1, s * 1, 0, s, s, false, false); // W
			c4x2.copyRect(img, s * 2, s * 1, s * 0, 0, s, s, false, false); // E
			c4x2.close();
			return img;
		}

		VidLib.LOGGER.error("Invalid source skybox texture from " + name + " Must be a cube map 3x2, 4x4, 4x3 or a vertical gradient, resolution was instead " + srcW + " x " + srcH);

		var img = new NativeImage(16, 16, false);
		img.fillRect(0, 0, 16, 16, 0xFF000000);
		return img;
	}

	@Override
	public void dumpContents(Identifier id, Path path) {
		TextureUtil.writeAsPNG(path, id.toDebugFileName(), getTexture(), 0, IntUnaryOperator.identity());
	}
}
