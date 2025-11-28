package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.util.UndashedUuid;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.util.MiscUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.Executor;

public interface PlayerHeads {
	ResourceLocation DEFAULT_HEAD_TEXTURE = VidLib.id("textures/misc/unknown_player.png");
	Gallery GALLERY = new Gallery("player_heads", () -> VidLibPaths.USER.get().resolve("player-heads"), TriState.TRUE);

	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	static void reload(TextureManager manager, Executor backgroundExecutor, Executor gameExecutor) throws IOException {
		GALLERY.load(manager);
	}

	static GalleryImage get(Minecraft mc, UUID uuid, String name) {
		var img = GALLERY.images.get(uuid);

		if (img == null) {
			img = GALLERY.createDummy(uuid, name);
			GALLERY.images.put(uuid, img);
			var url = "https://cravatar.eu/helmhead/" + UndashedUuid.toString(uuid) + "/256.png";

			try {
				img = GALLERY.upload(
					mc,
					uuid,
					() -> {
						var req = MiscUtils.HTTP_CLIENT.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofInputStream());

						if (req.statusCode() / 100 != 2) {
							throw new IllegalStateException("Player Head request " + url + " returned " + req.statusCode());
						}

						return req.body();
					},
					() -> name,
					() -> url,
					ImagePreProcessor.NONE
				);
			} catch (Exception ex) {
				VidLib.LOGGER.info("Failed to download player head of " + uuid);
			}
		}

		return img;
	}

	static AbstractTexture getTexture(Minecraft mc, @Nullable UUID uuid, String name) {
		if (uuid == null || uuid.equals(Util.NIL_UUID)) {
			return mc.getTextureManager().getTexture(DEFAULT_HEAD_TEXTURE);
		}

		return get(mc, uuid, name).load(mc);
	}
}
