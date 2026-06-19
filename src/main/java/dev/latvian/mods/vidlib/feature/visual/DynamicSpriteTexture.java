package dev.latvian.mods.vidlib.feature.visual;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import dev.latvian.mods.vidlib.core.VLSpriteContents;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.auto.TextureReloadParams;
import dev.latvian.mods.vidlib.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.function.IntUnaryOperator;

public class DynamicSpriteTexture extends AbstractTexture implements Dumpable, EphemeralTexture {
	public static Identifier get(Minecraft mc, SpriteKey key) {
		if (key.atlas() == SpriteKey.SPECIAL) {
			return key.sprite().texturePath();
		}

		return mc.getSprite(key).vl$getDynamicSpriteTexture(mc).path;
	}

	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	public static void reload(TextureReloadParams params) {
		for (var tex : params.manager().byPath.values()) {
			if (tex instanceof TextureAtlas atlas) {
				for (var sprite : atlas.getTextures().values()) {
					sprite.vl$invalidateDynamicSpriteTexture();
				}
			}
		}
	}

	public final TextureAtlasSprite sprite;
	public final SpriteKey key;
	public final Identifier path;
	public boolean initialized;

	public DynamicSpriteTexture(TextureAtlasSprite sprite) {
		this.sprite = sprite;
		this.key = SpriteKey.of(MiscUtils.assetFromPNG(sprite.atlasLocation()), new ClientAsset.ResourceTexture(sprite.contents().name()));
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
		texture = device.createTexture(path::toString, GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST, TextureFormat.RGBA8, w, h, 1, m);
		textureView = device.createTextureView(texture);

		for (int i = 0; i < m; i++) {
			contents.uploadFirstFrame(texture, i);
		}
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
