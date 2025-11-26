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
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class Gallery {
	public final String id;
	public final Supplier<Path> directory;
	public final Map<UUID, GalleryImage> images;

	public Gallery(String id, Supplier<Path> directory) {
		this.id = id;
		this.directory = directory;
		this.images = new ConcurrentHashMap<>();
	}

	public ResourceLocation id(UUID imageId) {
		return VidLib.id("textures/vidlib/generated/gallery/" + id + "/" + UndashedUuid.toString(imageId) + ".png");
	}

	@Nullable
	public GalleryImage get(UUID imageId) {
		return imageId == null ? null : images.get(imageId);
	}

	public void load(TextureManager manager, Executor backgroundExecutor, Executor gameExecutor) throws IOException {
		images.clear();

		var dir = directory.get();

		var futures = new ArrayList<CompletableFuture<Void>>();

		if (Files.exists(dir)) {
			try (var stream = Files.list(dir)) {
				for (var file : stream.filter(Files::isRegularFile).toList()) {
					futures.add(loadFile(manager, file, backgroundExecutor, gameExecutor));
				}
			}
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> VidLib.LOGGER.info("Loaded " + images.size() + " gallery '" + id + "' images"));
	}

	private CompletableFuture<Void> loadFile(TextureManager manager, Path file, Executor backgroundExecutor, Executor gameExecutor) {
		var fileName = file.getFileName().toString();

		if (fileName.endsWith(".png")) {
			fileName = fileName.substring(0, fileName.length() - 4);
			var nameParts = fileName.split("-", 2);

			if (nameParts.length == 2) {
				return CompletableFuture.supplyAsync(() -> {
					try (var in = Files.newInputStream(file)) {
						return NativeImage.read(in);
					} catch (Exception ex) {
						VidLib.LOGGER.info("Failed to load gallery " + id + " image " + file.toAbsolutePath(), ex);
						return null;
					}
				}, backgroundExecutor).thenAcceptAsync(image -> {
					if (image == null) {
						return;
					}

					var uuid = UndashedUuid.fromString(nameParts[0]);
					var textureId = id(uuid);
					var texture = new DynamicTexture(textureId::toString, image);
					texture.setFilter(true, false);
					manager.register(textureId, texture);
					images.put(uuid, new GalleryImage(this, uuid, nameParts[1], file, textureId));
				}, gameExecutor);
			}
		}

		return CompletableFuture.completedFuture(null);
	}

	public GalleryImage upload(Minecraft mc, Path src, ImagePreProcessor preProcessor) throws IOException {
		try (var in = Files.newInputStream(src)) {
			var originalImg = NativeImage.read(in);
			var img = preProcessor.apply(originalImg);
			var uuid = UUID.randomUUID();

			var displayName = StringUtils.normalizeFileName(src.getFileName().toString());

			if (displayName.isEmpty()) {
				displayName = "unknown";
			}

			var textureId = id(uuid);
			var dir = directory.get();

			if (Files.notExists(dir)) {
				Files.createDirectories(dir);
			}

			var dst = dir.resolve(UndashedUuid.toString(uuid) + "-" + displayName + ".png");
			img.writeToFile(dst);

			var texture = new DynamicTexture(textureId::toString, img);
			texture.setFilter(true, false);
			mc.getTextureManager().register(textureId, texture);

			var galleryImage = new GalleryImage(this, uuid, displayName, dst, textureId);
			images.put(uuid, galleryImage);

			VidLib.LOGGER.info("Uploaded " + src.toAbsolutePath() + " as " + dst.getFileName() + " to gallery '" + id + "'");
			return galleryImage;
		}
	}
}
