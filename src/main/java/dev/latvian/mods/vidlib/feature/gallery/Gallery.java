package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class Gallery {
	public final String id;
	public final Supplier<Path> directory;
	public final TriState blur;
	public final Map<UUID, GalleryImage> images;

	public Gallery(String id, Supplier<Path> directory, TriState blur) {
		this.id = id;
		this.directory = directory;
		this.blur = blur;
		this.images = new ConcurrentHashMap<>();
	}

	public ResourceLocation id(UUID imageId) {
		return VidLib.id("textures/vidlib/generated/gallery/" + id + "/" + UndashedUuid.toString(imageId) + ".png");
	}

	public Path path(UUID imageId, String displayName) throws IOException {
		var dir = directory.get();

		if (Files.notExists(dir)) {
			Files.createDirectories(dir);
		}

		return dir.resolve(UndashedUuid.toString(imageId) + "-" + displayName + ".png");
	}

	public GalleryImage createDummy(UUID imageId, String displayName) {
		return new GalleryImage(this, imageId, displayName, null, id(imageId));
	}

	@Nullable
	public GalleryImage get(UUID imageId) {
		return imageId == null ? null : images.get(imageId);
	}

	public void load(TextureManager manager) throws IOException {
		for (var image : images.values()) {
			if (manager.byPath.remove(image.textureId()) instanceof DynamicTexture tex) {
				tex.close();
			}
		}

		images.clear();

		var dir = directory.get();

		if (Files.exists(dir)) {
			try (var stream = Files.list(dir)) {
				for (var file : stream.filter(Files::isRegularFile).toList()) {
					var fileName = file.getFileName().toString();

					if (fileName.endsWith(".png")) {
						fileName = fileName.substring(0, fileName.length() - 4);
						var nameParts = fileName.split("-", 2);

						if (nameParts.length == 2) {
							var uuid = UndashedUuid.fromString(nameParts[0]);
							var textureId = id(uuid);
							images.put(uuid, new GalleryImage(this, uuid, nameParts[1], file, textureId));
						}
					}
				}
			}
		}

		VidLib.LOGGER.info("Loaded " + images.size() + " gallery '" + id + "' images");
	}

	public GalleryImage upload(Minecraft mc, UUID uuid, Path src, ImagePreProcessor preProcessor) throws Exception {
		return upload(
			mc,
			uuid,
			() -> Files.newInputStream(src),
			() -> StringUtils.normalizeFileName(src.getFileName().toString()),
			() -> src.toAbsolutePath().toString(),
			preProcessor
		);
	}

	public GalleryImage upload(Minecraft mc, UUID uuid, Callable<InputStream> input, Supplier<String> displayNameGetter, Supplier<String> fullPath, ImagePreProcessor preProcessor) throws Exception {
		try (var in = input.call()) {
			var originalImg = NativeImage.read(in);
			var img = preProcessor.apply(originalImg);

			var displayName = displayNameGetter.get();

			if (displayName.isEmpty()) {
				displayName = "unknown";
			}

			var textureId = id(uuid);
			var dst = path(uuid, displayName);
			img.writeToFile(dst);

			var texture = new DynamicTexture(textureId::toString, img);
			texture.setFilter(blur, false);
			mc.getTextureManager().register(textureId, texture);

			var galleryImage = new GalleryImage(this, uuid, displayName, dst, textureId);
			images.put(uuid, galleryImage);

			VidLib.LOGGER.info("Uploaded " + fullPath.get() + " as " + dst.getFileName() + " to gallery '" + id + "'");
			return galleryImage;
		}
	}
}
