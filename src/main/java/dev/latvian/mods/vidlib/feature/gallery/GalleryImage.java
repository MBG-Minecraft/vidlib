package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.platform.NativeImage;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public record GalleryImage(
	Gallery gallery,
	UUID id,
	String displayName,
	@Nullable Path path,
	ResourceLocation textureId
) {
	public static final ResourceLocation LOADING_TEXTURE = VidLib.id("textures/misc/loading.png");

	public boolean delete() {
		try {
			if (path != null) {
				Files.deleteIfExists(path);
			}

			gallery.images.remove(id);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public AbstractTexture load(Minecraft mc) {
		var tex = mc.getTextureManager().byPath.get(textureId);

		if (tex == null) {
			tex = mc.getTextureManager().getTexture(LOADING_TEXTURE);
			mc.getTextureManager().byPath.put(textureId, tex);

			if (path != null && Files.exists(path)) {
				Util.backgroundExecutor().execute(() -> {
					try (var in = Files.newInputStream(path)) {
						var image = NativeImage.read(in);

						mc.execute(() -> {
							var texture = new DynamicTexture(textureId::toString, image);
							texture.setFilter(gallery.blur, false);
							mc.getTextureManager().byPath.put(textureId, texture);
						});
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				});
			}
		}

		return tex;
	}
}
