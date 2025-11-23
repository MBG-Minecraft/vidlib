package dev.latvian.mods.vidlib.feature.visual;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import dev.latvian.mods.vidlib.core.VLSpriteContents;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.function.IntUnaryOperator;

public class DynamicSpriteTexture extends AbstractTexture implements Dumpable, EphemeralTexture {
	public static ResourceLocation get(Minecraft mc, SpriteKey key) {
		if (key.atlas() == SpriteKey.SPECIAL) {
			return key.sprite();
		}

		return mc.getSprite(key).vl$getDynamicSpriteTexture(mc).path;
	}

	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	public static void reload(TextureManager textureManager) {
		for (var tex : textureManager.byPath.values()) {
			if (tex instanceof TextureAtlas atlas) {
				for (var sprite : atlas.getTextures().values()) {
					sprite.vl$invalidateDynamicSpriteTexture();
				}
			}
		}
	}

	public final TextureAtlasSprite sprite;
	public final SpriteKey key;
	public final ResourceLocation path;
	public boolean initialized;

	public DynamicSpriteTexture(TextureAtlasSprite sprite) {
		this.sprite = sprite;
		this.key = SpriteKey.of(sprite.atlasLocation(), sprite.contents().name());
		this.path = key.dynamic();
	}

	public void initialize(Minecraft mc) {
		var contents = sprite.contents();
		int mipLevel = mc.options.mipmapLevels().get();

		if (mipLevel > 0) {
			contents.increaseMipLevel(mipLevel);
		}

		int w = contents.width();
		int h = contents.height();
		int m = contents.byMipLevel.length;

		var device = RenderSystem.getDevice();
		texture = device.createTexture(path::toString, TextureFormat.RGBA8, w, h, m);
		setFilter(false, true);
		setClamp(false);
		contents.uploadFirstFrame(0, 0, texture);
	}

	@Override
	public void dumpContents(ResourceLocation id, Path path) {
		if (texture != null) {
			TextureUtil.writeAsPNG(path, id.toDebugFileName(), texture, 0, IntUnaryOperator.identity());
		}
	}

	@Override
	public void close() {
		super.close();
		((VLSpriteContents) sprite.contents()).vl$setDynamicSpriteTexture(null);
		initialized = false;
	}
}
