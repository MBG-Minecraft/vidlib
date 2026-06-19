package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.platform.NativeImage;
import dev.latvian.mods.vidlib.feature.client.ConfiguredDynamicTexture;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

public record GalleryImage<K>(
	GalleryImageKey<K> key,
	String displayName,
	@Nullable Path path,
	Identifier textureId
) {
	public boolean deleteFile() {
		try {
			if (path != null) {
				Files.deleteIfExists(path);
			}

			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public AbstractTexture load(Minecraft mc, boolean blocking) {
		var tex = mc.getTextureManager().byPath.get(textureId);

		if (tex == null) {
			if (blocking) {
				tex = loadNow(mc, true);

				if (tex == null) {
					tex = mc.getTextureManager().getTexture(VidLibTextures.LOADING.texturePath());
					mc.getTextureManager().byPath.put(textureId, tex);
				}
			} else {
				tex = mc.getTextureManager().getTexture(VidLibTextures.LOADING.texturePath());
				mc.getTextureManager().byPath.put(textureId, tex);
				Util.backgroundExecutor().execute(() -> loadNow(mc, false));
			}
		}

		return tex;
	}

	@Nullable
	private AbstractTexture loadNow(Minecraft mc, boolean blocking) {
		if (path != null && Files.exists(path)) {
			try (var in = Files.newInputStream(path)) {
				var image = NativeImage.read(in);

				if (blocking) {
					var texture = new ConfiguredDynamicTexture(textureId::toString, image, true, key.gallery().blur);
					mc.getTextureManager().byPath.put(textureId, texture);
					return texture;
				} else {
					mc.execute(() -> {
						var texture = new ConfiguredDynamicTexture(textureId::toString, image, true, key.gallery().blur);
						mc.getTextureManager().byPath.put(textureId, texture);
					});
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return null;
	}
}
