package dev.latvian.mods.vidlib.feature.gallery;

import dev.latvian.mods.klib.util.PathIDGenerator;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.imgui.AsyncFileSelector;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Files;
import java.nio.file.Path;

public record GalleryFileUploader<K>(PathIDGenerator<K> randomId, ImagePreProcessor preProcessor) implements GalleryUploader<K> {
	@Override
	public ResourceLocation getIcon() {
		return VidLibTextures.FOLDER;
	}

	@Override
	public String getTooltip() {
		return "Open";
	}

	@Override
	public void render(Gallery<K> gallery, GalleryImageImBuilder builder, ImGraphics graphics, boolean clicked) {
		if (clicked) {
			AsyncFileSelector.openFileDialog(null, "Select Pin Image", "png").thenAccept(pathString -> {
				var path = pathString == null ? null : Path.of(pathString);

				if (path != null && Files.exists(path) && Files.isRegularFile(path)) {
					graphics.mc.execute(() -> {
						try {
							builder.set(gallery.upload(graphics.mc, randomId.generate(path), path, preProcessor));
							builder.fullUpdate = true;
						} catch (Exception ex) {
							throw new RuntimeException(ex);
						}
					});
				}
			});
		}
	}
}