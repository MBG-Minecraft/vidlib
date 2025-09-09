package dev.latvian.mods.vidlib.feature.decal;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.DynamicTextureHolder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;
import java.util.List;
import java.util.function.IntUnaryOperator;

public class DecalTexture extends AbstractTexture implements Dumpable {
	public static final ResourceLocation ID = VidLib.id("textures/effect/decals.png");

	@ClientAutoRegister
	public static final DynamicTextureHolder<DecalTexture> HOLDER = new DynamicTextureHolder<>(ID, Lazy.of(DecalTexture::new));

	private IntArrayList[] pixels;
	private NativeImage image;

	public DecalTexture() {
		this.texture = RenderSystem.getDevice().createTexture("Decal Texture", TextureFormat.RGBA8, 16, 16, 1);
		texture.setTextureFilter(FilterMode.LINEAR, false);
		this.pixels = new IntArrayList[16];

		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = new IntArrayList(16);
		}
	}

	@Override
	public void close() {
		super.close();

		if (image != null) {
			image.close();
			image = null;
		}
	}

	public void update(List<Decal> decals, Vec3 cameraPos) {
		if (decals.size() > pixels.length) {
			var newPixels = new IntArrayList[Mth.ceil((decals.size() + 1) / 4D) * 4];
			System.arraycopy(pixels, 0, newPixels, 0, pixels.length);

			for (int i = pixels.length; i < newPixels.length; i++) {
				newPixels[i] = new IntArrayList(16);
			}

			pixels = newPixels;
		}

		for (int i = 0; i < decals.size(); i++) {
			var decal = decals.get(i);
			pixels[i].clear();
			decal.upload(pixels[i], cameraPos);
		}

		if (image == null || pixels.length > image.getHeight()) {
			if (image != null) {
				image.close();
				image = null;
			}

			this.image = new NativeImage(16, pixels.length, false);

			if (texture != null) {
				texture.close();
			}

			texture = RenderSystem.getDevice().createTexture("Decal Texture", TextureFormat.RGBA8, image.getWidth(), image.getHeight(), 1);
			texture.setTextureFilter(FilterMode.LINEAR, false);
		}

		for (int y = 0; y < pixels.length; y++) {
			for (int x = 0; x < pixels[y].size(); x++) {
				image.setPixel(x, y, pixels[y].getInt(x));
			}
		}

		RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, image);
	}

	@Override
	public void dumpContents(ResourceLocation id, Path path) {
		TextureUtil.writeAsPNG(path, id.toDebugFileName(), getTexture(), 0, IntUnaryOperator.identity());
	}
}
