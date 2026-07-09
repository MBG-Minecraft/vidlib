package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import dev.latvian.mods.klib.color.Color;
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
import net.minecraft.world.attribute.EnvironmentAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.IntUnaryOperator;

public class SkyboxTexture extends ReloadableTexture implements Dumpable {
	public final ClientAtmosphere atmosphere;
	public int resolution;
	private ImIcon icon;

	public SkyboxTexture(Identifier id, ClientAtmosphere atmosphere) {
		super(id);
		this.atmosphere = atmosphere;
	}

	@Override
	public void close() {
		super.close();
		atmosphere.skyboxTexture = null;
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
		if (atmosphere.skybox == null) {
			var src = new NativeImage(4, 512, false);
			var topCol = Color.ofRGB(atmosphere.attributes.applyModifier(EnvironmentAttributes.SKY_COLOR, 0x000000));
			var bottomCol = Color.ofRGB(atmosphere.attributes.applyModifier(EnvironmentAttributes.FOG_COLOR, 0xFFFFFF));

			for (int y = 0; y < 512; y++) {
				int col = topCol.lerp(y / 511F, bottomCol).argb();

				for (int x = 0; x < 4; x++) {
					src.setPixel(x, y, col);
				}
			}

			var image = process(atmosphere.id, src, src.getWidth(), src.getHeight());
			resolution = image.getHeight() / 2;
			return new TextureContents(image, new TextureMetadataSection(false, false, MipmapStrategy.AUTO, 0F));
		}

		try (var in = manager.getResource(atmosphere.skybox.assetId().texturePath()).orElseThrow().open()) {
			var src = NativeImage.read(in);
			var image = process(atmosphere.skybox.assetId().id(), src, src.getWidth(), src.getHeight());
			resolution = image.getHeight() / 2;
			return new TextureContents(image, new TextureMetadataSection(false, false, MipmapStrategy.AUTO, 0F));
		}
	}

	@SuppressWarnings({"ConstantValue", "PointlessArithmeticExpression"})
	public static NativeImage process(Identifier id, NativeImage src, int srcW, int srcH) {
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

			src.close();
			return img;
		} else if (srcH > srcW) { // Vertical Gradient
			var img = new NativeImage(srcH * 4, srcH * 2, false);

			var g = new int[srcH];

			for (var y = 0; y < srcH; y++) {
				g[y] = src.getPixel(0, y);
			}

			src.close();

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

		VidLib.LOGGER.error("Invalid source skybox texture from " + id + " Must be either foldable 4x4, 4x3 or a vertical gradient");

		var img = new NativeImage(16, 16, false);
		img.fillRect(0, 0, 16, 16, 0xFF000000);
		return img;
	}

	@Override
	public void dumpContents(Identifier id, Path path) {
		TextureUtil.writeAsPNG(path, id.toDebugFileName(), getTexture(), 0, IntUnaryOperator.identity());
	}
}
