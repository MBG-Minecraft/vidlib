package dev.latvian.mods.vidlib.feature.visual;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import dev.latvian.mods.klib.texture.SpriteKey;
import dev.latvian.mods.vidlib.core.VLSpriteContents;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.auto.TextureReloadParams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.function.IntUnaryOperator;

public class DynamicSpriteTexture extends AbstractTexture implements Dumpable, EphemeralTexture {
	public static Identifier get(Minecraft mc, SpriteKey key) {
		if (key.atlas() == SpriteKey.SPECIAL_ATLAS) {
			return key.sprite().id();
		}

		return mc.getSprite(key).vl$getDynamicSpriteTexture(mc).path;
	}

	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	public static void reload(TextureReloadParams params) {
		// 26.3's TextureAtlas no longer exposes its sprite list; every dynamic sprite texture is
		// itself registered in the texture manager, so invalidating them directly is equivalent.
		for (var tex : params.manager().byPath.values()) {
			if (tex instanceof DynamicSpriteTexture dst) {
				((VLSpriteContents) dst.sprite.contents()).vl$setDynamicSpriteTexture(null);
				dst.initialized = false;
			}
		}
	}

	public final TextureAtlasSprite sprite;
	public final SpriteKey key;
	public final Identifier path;
	public boolean initialized;

	public DynamicSpriteTexture(TextureAtlasSprite sprite) {
		this.sprite = sprite;
		this.key = SpriteKey.of(sprite.atlasLocation(), sprite.contents().name());
		this.path = key.dynamic();
	}

	public void initialize(Minecraft mc) {
		SpriteContents contents = sprite.contents();
		int mipLevel = mc.options.mipmapLevels().get();

		if (mipLevel > 0) {
			contents.increaseMipLevel(mipLevel);
		}

		int w = contents.width();
		int h = contents.height();
		int m = mipLevel > 0 ? mipLevel + 1 : 1;

		var device = RenderSystem.getDevice();
		texture = device.createTexture(path::toString, com.mojang.renderpearl.api.textures.GpuTexture.USAGE_TEXTURE_BINDING, GpuFormat.RGBA8_UNORM, w, h, 1, m);
		textureView = device.createTextureView(texture);
		sampler = RenderSystem.getSamplerCache()
			.getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.NEAREST, FilterMode.LINEAR, m > 1);
		contents.uploadFirstFrame(texture, 0);
	}

	@Override
	public void dumpContents(Identifier id, Path path) {
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
