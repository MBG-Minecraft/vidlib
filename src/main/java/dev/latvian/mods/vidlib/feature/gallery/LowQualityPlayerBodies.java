package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.platform.NativeImage;
import dev.latvian.mods.klib.util.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface LowQualityPlayerBodies {
	@ClientAutoRegister
	Gallery<UUID> GALLERY = Gallery.ofUUIDKey("low_quality_player_bodies", () -> VidLibPaths.USER.get().resolve("low-quality-player-bodies"), TriState.TRUE);

	static GalleryImage<UUID> get(Minecraft mc, UUID uuid) {
		return GALLERY.getRender(mc, uuid, PlayerProfiles::getName, LowQualityPlayerBodies::render, ImagePreProcessor.NONE);
	}

	private static NativeImage render(Minecraft mc, UUID uuid, String name) {
		return PlayerBodies.render(64, mc, PlayerSkins.getSkin(mc, uuid, true), true);
	}

	static AbstractTexture getTexture(Minecraft mc, @Nullable UUID uuid) {
		if (uuid == null || uuid.equals(Util.NIL_UUID)) {
			return mc.getTextureManager().getTexture(VidLibTextures.DEFAULT_PLAYER_BODY.id());
		}

		return get(mc, uuid).load(mc, false);
	}
}
