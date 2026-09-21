package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.latvian.mods.klib.io.CompressionMethod;
import dev.latvian.mods.klib.io.checksum.MD5;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.util.MiscUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class URITextures {
	public static class Entry implements Supplier<String> {
		public final URI uri;
		public final ClientAsset asset;
		public DynamicTexture texture;

		private Entry(URI uri, ClientAsset asset) {
			this.uri = uri;
			this.asset = asset;
		}

		@Override
		public String get() {
			return uri.toString();
		}
	}

	private static final Map<URI, Entry> CACHE = new HashMap<>();

	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	public static void reload() {
		CACHE.clear();
	}

	@Nullable
	public static NativeImage load(URI uri) throws IOException, InterruptedException {
		if (!uri.isAbsolute()) {
			return null;
		}

		try (var in = Files.newInputStream(Path.of(uri))) {
			return NativeImage.read(in);
		} catch (Exception ignored) {
		}

		var response = MiscUtils.sendRetrying(MiscUtils.HTTP_CLIENT, HttpRequest.newBuilder(uri).header("Accept-Encoding", "zstd, gzip, deflate, br").GET().build(), HttpResponse.BodyHandlers.ofInputStream());
		int code = response.statusCode();

		if (code == 404) {
			return null;
		}

		var bytes = new byte[0];

		var encoding = response.headers().firstValue("Content-Encoding").orElse("");

		try (var in = CompressionMethod.of(encoding).in(response.body())) {
			bytes = in.readAllBytes();
		}

		boolean binary = bytes.length == 0;

		for (byte b : bytes) {
			if (b <= 0) {
				binary = true;
				break;
			}
		}

		try {
			if (code / 100 == 2) {
				try {
					return NativeImage.read(bytes);
				} catch (IOException ex) {
					if (ex.getMessage().equals("Bad PNG Signature")) {
						var img = ImageIO.read(new ByteArrayInputStream(bytes));
						var nImg = new NativeImage(img.getWidth(), img.getHeight(), false);

						for (int y = 0; y < img.getHeight(); y++) {
							for (int x = 0; x < img.getWidth(); x++) {
								nImg.setPixel(x, y, img.getRGB(x, y));
							}
						}

						return nImg;
					} else {
						throw ex;
					}
				}
			}
		} catch (Exception ex) {
			var message = binary ? (bytes.length + "bytes of binary data") : new String(bytes, StandardCharsets.UTF_8);
			throw new IOException("HTTP error " + code + ": " + message, ex);
		}

		var message = binary ? (bytes.length + "bytes of binary data") : new String(bytes, StandardCharsets.UTF_8);
		throw new IOException("HTTP error " + code + ": " + message);
	}

	public static Entry get(Minecraft mc, URI uri) {
		return CACHE.computeIfAbsent(uri, key -> {
			var hash = MD5.TYPE.digest(key.toString().getBytes(StandardCharsets.UTF_8)).toString();
			var entry = new Entry(key, new ClientAsset(VidLib.id("uri/" + hash)));
			entry.texture = new DynamicTexture(entry, BuiltInImages.loading());

			mc.getTextureManager().register(entry.asset.texturePath(), entry.texture);

			if (key.isAbsolute()) {
				Util.backgroundExecutor().execute(() -> {
					try {
						var image = load(entry.uri);

						if (image != null) {
							mc.execute(() -> {
								entry.texture = new DynamicTexture(entry, image);
								mc.getTextureManager().register(entry.asset.texturePath(), entry.texture);
							});
						}
					} catch (Exception ex) {
						VidLib.LOGGER.warn("Failed to fetch texture from " + entry.uri, ex);
					}
				});
			}

			return entry;
		});
	}

	public static ResourceLocation getTexture(Minecraft mc, URI uri) {
		return get(mc, uri).asset.texturePath();
	}

	public static int gl(Minecraft mc, @Nullable URI uri, ResourceLocation fallback) {
		if (uri != null) {
			return URITextures.get(mc, uri).texture.getTexture().vl$getHandle();
		} else {
			return mc.getTextureManager().getTexture(fallback).getTexture().vl$getHandle();
		}
	}
}
