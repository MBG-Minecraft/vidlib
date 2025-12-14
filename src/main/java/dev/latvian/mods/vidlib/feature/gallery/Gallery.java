package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.util.MiscUtils;
import dev.latvian.mods.vidlib.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Gallery<K> {
	public static final Lazy<Map<String, Gallery<?>>> ALL = Lazy.map(map -> {
		for (var s : ClientAutoRegister.SCANNED.get()) {
			if (s.value() instanceof Gallery<?> gallery) {
				map.put(gallery.id, gallery);
			}
		}
	});

	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	public static void reload(TextureManager manager, Executor backgroundExecutor, Executor gameExecutor) throws IOException {
		for (var gallery : ALL.get().values()) {
			gallery.load(manager);
		}
	}

	public static Gallery<UUID> ofUUIDKey(String id, Supplier<Path> directory, TriState blur) {
		return new Gallery<>(id, directory, blur, UndashedUuid::toString, UndashedUuid::fromStringLenient);
	}

	public final String id;
	public final Supplier<Path> directory;
	public final TriState blur;
	public final Function<K, String> keyToString;
	public final Function<String, K> stringToKey;
	public final Map<K, GalleryImage<K>> images;

	public Gallery(String id, Supplier<Path> directory, TriState blur, Function<K, String> keyToString, @Nullable Function<String, K> stringToKey) {
		this.id = id;
		this.directory = directory;
		this.blur = blur;
		this.keyToString = keyToString;
		this.stringToKey = stringToKey;
		this.images = new ConcurrentHashMap<>();
	}

	public ResourceLocation id(K imageId) {
		return VidLib.id("textures/vidlib/generated/gallery/" + id + "/" + keyToString.apply(imageId) + ".png");
	}

	@Nullable
	public Path newPath(K imageId, String displayName) throws IOException {
		var dir = directory.get();

		if (dir == null) {
			return null;
		}

		if (Files.notExists(dir)) {
			Files.createDirectories(dir);
		}

		var fileName = keyToString.apply(imageId) + (displayName.isEmpty() ? "" : ("-" + displayName));
		var finalFileName = fileName + ".png";

		var path = dir.resolve(finalFileName);
		/*
		int count = 2;

		while (Files.exists(path)) {
			finalFileName = fileName + "-" + count + ".png";
			count++;
			path = dir.resolve(finalFileName);
		}
		 */

		return path;
	}

	public GalleryImage<K> createDummy(K imageId, String displayName) {
		return new GalleryImage<>(this, imageId, displayName, null, id(imageId));
	}

	@Nullable
	public GalleryImage<K> get(K imageId) {
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

		if (stringToKey != null && Files.exists(dir)) {
			try (var stream = Files.list(dir)) {
				for (var file : stream.filter(Files::isRegularFile).toList()) {
					var fileName = file.getFileName().toString();

					if (fileName.endsWith(".png")) {
						fileName = fileName.substring(0, fileName.length() - 4);
						var nameParts = fileName.split("-", 2);

						if (nameParts.length == 2) {
							var uuid = stringToKey.apply(nameParts[0]);
							var textureId = id(uuid);
							images.put(uuid, new GalleryImage<>(this, uuid, nameParts[1], file, textureId));
						}
					}
				}
			}
		}

		VidLib.LOGGER.info("Loaded " + images.size() + " gallery '" + id + "' images");
	}

	public GalleryImage<K> upload(Minecraft mc, K uuid, Path src, ImagePreProcessor preProcessor) throws Exception {
		return upload(
			mc,
			uuid,
			() -> {
				try (var in = Files.newInputStream(src)) {
					return NativeImage.read(in);
				}
			},
			StringUtils.normalizeFileName(src.getFileName().toString()),
			() -> src.toAbsolutePath().toString(),
			preProcessor
		);
	}

	public GalleryImage<K> upload(Minecraft mc, K key, Callable<NativeImage> input, String displayName, Supplier<String> fullPath, ImagePreProcessor preProcessor) throws Exception {
		var originalImg = input.call();
		var img = preProcessor.apply(originalImg);

		var textureId = id(key);
		var dst = newPath(key, displayName);

		if (dst != null) {
			img.writeToFile(dst);
		}

		var texture = new DynamicTexture(textureId::toString, img);
		texture.setFilter(blur, false);
		mc.getTextureManager().register(textureId, texture);

		var galleryImage = new GalleryImage<>(this, key, displayName, dst, textureId);
		images.put(key, galleryImage);

		if (dst != null) {
			VidLib.LOGGER.info("Uploaded " + fullPath.get() + " as " + dst.getFileName() + " to gallery '" + id + "'");
		}

		return galleryImage;
	}

	public GalleryImage<K> getRemote(Minecraft mc, K key, Function<K, String> nameGetter, BiFunction<K, String, String> urlGetter, ImagePreProcessor preProcessor) {
		var img = images.get(key);

		if (img != null) {
			return img;
		}

		var name = nameGetter.apply(key);
		img = createDummy(key, name);
		images.put(key, img);
		var url = urlGetter.apply(key, name);

		if (url == null || url.isEmpty()) {
			return img;
		}

		try {
			return upload(
				mc,
				key,
				() -> {
					var req = MiscUtils.HTTP_CLIENT.send(HttpRequest.newBuilder(URI.create(url))
						.GET()
						.header("User-Agent", "VidLib/" + VidLib.VERSION)
						.build(), HttpResponse.BodyHandlers.ofInputStream());

					if (req.statusCode() / 100 != 2) {
						throw new IllegalStateException("Request " + url + " returned " + req.statusCode());
					}

					try (var in = req.body()) {
						return NativeImage.read(in);
					}
				},
				name,
				() -> url,
				preProcessor
			);
		} catch (Exception ex) {
			VidLib.LOGGER.warn("Failed to download " + id + "/" + key, ex);
			return img;
		}
	}

	public GalleryImage<K> getRender(Minecraft mc, K uuid, Function<K, String> nameSupplier, GalleryImageRenderCallback<K> render, ImagePreProcessor preProcessor) {
		// return GALLERY.getRemote(mc, uuid, name, id -> "https://mc-heads.net/body/" + UndashedUuid.toString(id) + "/420", PRE_PROCESSOR);

		var img = images.get(uuid);

		if (img == null) {
			var name = nameSupplier.apply(uuid);
			img = createDummy(uuid, name);
			images.put(uuid, img);

			try {
				img = upload(
					mc,
					uuid,
					() -> render.render(mc, uuid, name),
					name,
					() -> "render/" + id + "/" + uuid + (name.isEmpty() ? "" : ("/" + name)),
					preProcessor
				);
			} catch (Exception ex) {
				VidLib.LOGGER.warn("Failed to render " + id + "/" + uuid, ex);
			}
		}

		return img;
	}
}
