package dev.latvian.mods.vidlib.feature.visual;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.serialization.DataResult;
import com.mojang.util.UndashedUuid;
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

	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	public static void reload() {
		ALL.clear();
	}

	public static PlayerHeadTexture get(UUID uuid) {
		var tex = ALL.get(uuid);

		if (tex == null) {
			tex = new PlayerHeadTexture(uuid);
			var mc = Minecraft.getInstance();
			mc.getTextureManager().register(tex.path, tex);
			ALL.put(uuid, tex);

			try {
				tex.load();
			} catch (Exception ex) {
				VidLib.LOGGER.error("Failed to load PlayerHeadTexture " + uuid, ex);
			}
		}

		return tex;
	}

	public final UUID uuid;
	public final ResourceLocation path;

	private PlayerHeadTexture(UUID uuid) {
		this.uuid = uuid;
		this.path = VidLib.id("textures/vidlib/generated/player_head/" + UndashedUuid.toString(uuid) + ".png");
	}

	private void load() throws Exception {
		var result = MiscUtils.fetch("https://crafatar.com/renders/head/" + UndashedUuid.toString(uuid) + "?scale=3&default=MHF_Steve&overlay");
		var bytes = result.isSuccess() ? result.getOrThrow() : null;

		try (var dst = new NativeImage(64, 64, true)) {
			if (bytes != null) {
				try (var src = NativeImage.read(bytes)) {
					src.copyRect(dst, 0, 0, 2, 4, 60, 55, false, false);
				} catch (Exception ex) {
					VidLib.LOGGER.error("Failed to load player head texture for " + uuid, ex);
					dst.fillRect(0, 0, 64, 64, 0xFF000000);
				}
			} else {
				VidLib.LOGGER.error("Failed to load player head texture for " + uuid + ": " + result.error().map(DataResult.Error::message).orElse("Unknown"));
				dst.fillRect(0, 0, 64, 64, 0xFF000000);
			}

			var device = RenderSystem.getDevice();
			texture = device.createTexture(path::toString, TextureFormat.RGBA8, dst.getWidth(), dst.getHeight(), 1);
			setFilter(true, false);
			setClamp(true);
			device.createCommandEncoder().writeToTexture(texture, dst, 0, 0, 0, dst.getWidth(), dst.getHeight(), 0, 0);
		}
	}
}