package dev.latvian.mods.vidlib.feature.visual;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.serialization.DataResult;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerHeadTexture extends AbstractTexture {
	private static final Map<UUID, PlayerHeadTexture> ALL = new HashMap<>();
	public static final ResourceLocation DEFAULT_SKIN_TEXTURE = VidLib.id("textures/misc/unknown_player.png");

	private static final Lazy<NativeImage> DEFAULT_SKIN = Lazy.of(() -> {
		try (var in = Minecraft.getInstance().getResourceManager().getResourceOrThrow(DEFAULT_SKIN_TEXTURE).open()) {
			return NativeImage.read(in);
		} catch (Exception ex) {
			return null;
		}
	});

	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	public static void reload() {
		ALL.clear();
		DEFAULT_SKIN.forget(NativeImage::close);
	}

	public static PlayerHeadTexture get(UUID uuid) {
		var tex = ALL.get(uuid);

		if (tex == null) {
			tex = new PlayerHeadTexture(uuid);
			var mc = Minecraft.getInstance();
			mc.getTextureManager().register(tex.path, tex);
			ALL.put(uuid, tex);
			tex.load();
		}

		return tex;
	}

	public final UUID uuid;
	public final ResourceLocation path;

	private PlayerHeadTexture(UUID uuid) {
		this.uuid = uuid;
		this.path = VidLib.id("textures/vidlib/generated/player_head/" + UndashedUuid.toString(uuid) + ".png");
	}

	private void load() {
		try (var dst = new NativeImage(128, 128, true)) {
			var device = RenderSystem.getDevice();
			texture = device.createTexture(path::toString, TextureFormat.RGBA8, dst.getWidth(), dst.getHeight(), 1);
			setFilter(true, false);
			setClamp(true);

			var defaultSkin = DEFAULT_SKIN.get();

			if (defaultSkin != null) {
				defaultSkin.copyRect(dst, 0, 0, 0, 0, 128, 128, false, false);
			} else {
				dst.fillRect(0, 0, 128, 128, 0xFF000000);
			}

			device.createCommandEncoder().writeToTexture(texture, dst, 0, 0, 0, dst.getWidth(), dst.getHeight(), 0, 0);
		}

		Thread.startVirtualThread(() -> {
			try {
				var result = MiscUtils.fetch("https://crafatar.com/renders/head/" + UndashedUuid.toString(uuid) + "?scale=6&default=MHF_Steve&overlay");
				var bytes = result.isSuccess() ? result.getOrThrow() : null;

				Minecraft.getInstance().execute(() -> {
					try (var dst = new NativeImage(128, 128, true)) {
						if (bytes != null && bytes.length > 0) {
							try (var src = NativeImage.read(bytes)) {
								src.copyRect(dst, 0, 0, 4, 8, 120, 111, false, false);
								RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, dst, 0, 0, 0, dst.getWidth(), dst.getHeight(), 0, 0);
							} catch (Exception ex) {
								VidLib.LOGGER.error("Failed to load player head texture for " + uuid, ex);
							}
						} else {
							VidLib.LOGGER.error("Failed to load player head texture bytes for " + uuid + ": " + result.error().map(DataResult.Error::message).orElse("Unknown error"));
						}
					}
				});
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to load player head texture for " + uuid, ex);
			}
		});
	}
}