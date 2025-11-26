package dev.latvian.mods.vidlib.feature.gallery;

import net.minecraft.resources.ResourceLocation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public record GalleryImage(
	Gallery gallery,
	UUID id,
	String displayName,
	Path path,
	ResourceLocation textureId
) {
	public boolean delete() {
		try {
			Files.delete(path);
			gallery.images.remove(id);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
}
