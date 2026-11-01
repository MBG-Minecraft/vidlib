package dev.latvian.mods.vidlib.feature.gallery;

import dev.latvian.mods.klib.util.SkinTexture;
import dev.latvian.mods.klib.util.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfile;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PlayerSkins {
	@ClientAutoRegister
	Gallery<UUID> GALLERY = Gallery.ofUUIDKey("player_skins", () -> VidLibPaths.USER.get().resolve("player-skins"), TriState.TRUE);

	static PlayerSkin of(SkinTexture skin) {
		return PlayerSkin.insecure(skin.asset(), null, null, skin.slim() ? PlayerModelType.SLIM : PlayerModelType.WIDE);
	}

	PlayerSkin[] DEFAULT_WIDE_SKINS = new PlayerSkin[]{
		of(SkinTexture.WIDE_STEVE),
		of(SkinTexture.WIDE_ALEX),
		of(SkinTexture.WIDE_ARI),
		of(SkinTexture.WIDE_EFE),
		of(SkinTexture.WIDE_KAI),
		of(SkinTexture.WIDE_MAKENA),
		of(SkinTexture.WIDE_NOOR),
		of(SkinTexture.WIDE_SUNNY),
		of(SkinTexture.WIDE_ZURI)
	};

	PlayerSkin[] DEFAULT_SLIM_SKINS = new PlayerSkin[]{
		of(SkinTexture.SLIM_STEVE),
		of(SkinTexture.SLIM_ALEX),
		of(SkinTexture.SLIM_ARI),
		of(SkinTexture.SLIM_EFE),
		of(SkinTexture.SLIM_KAI),
		of(SkinTexture.SLIM_MAKENA),
		of(SkinTexture.SLIM_NOOR),
		of(SkinTexture.SLIM_SUNNY),
		of(SkinTexture.SLIM_ZURI)
	};

	static GalleryImage<UUID> get(Minecraft mc, UUID uuid) {
		return GALLERY.getRemote(mc, uuid, PlayerProfiles::getName, (id, n) -> PlayerProfiles.get(id).skinUrl().orElse(null), ImagePreProcessor.NONE);
	}

	static AbstractTexture getTexture(Minecraft mc, @Nullable UUID uuid) {
		if (uuid == null || uuid.equals(Util.NIL_UUID)) {
			return mc.getTextureManager().getTexture(DEFAULT_WIDE_SKINS[0].body().texturePath());
		}

		return get(mc, uuid).load(mc, false);
	}

	static PlayerModelType getModelType(@Nullable PlayerProfile profile) {
		if (profile == null || profile.isError()) {
			return PlayerModelType.WIDE;
		}

		return profile.slimModel() ? PlayerModelType.SLIM : PlayerModelType.WIDE;
	}

	static PlayerSkin getSkin(Minecraft mc, UUID uuid, boolean blocking) {
		var applied = getAppliedSkin(mc, uuid);

		if (applied != null) {
			return of(applied);
		} else if (uuid.equals(PlayerProfile.STEVE.profile().id())) {
			return DEFAULT_WIDE_SKINS[0];
		} else if (uuid.equals(PlayerProfile.ALEX.profile().id())) {
			return DEFAULT_SLIM_SKINS[1];
		} else {
			var profile = PlayerProfiles.get(uuid);
			var modelType = PlayerSkins.getModelType(profile);
			var skin = PlayerSkins.get(mc, uuid);
			skin.load(mc, blocking);
			// Gallery image ids ARE full texture paths (e.g. vidlib:textures/vidlib/generated/gallery/...png),
			// so the texture path must be used verbatim - the single-arg ResourceTexture ctor would
			// prepend "textures/" and append ".png" again, producing an unloadable id.
			var body = new net.minecraft.core.ClientAsset.ResourceTexture(skin.textureId(), skin.textureId());
			dev.latvian.mods.vidlib.VidLib.LOGGER.info("[skindbg] {} id={} body.texturePath={}", uuid, skin.textureId(), body.texturePath());
			return PlayerSkin.insecure(body, null, null, modelType);
		}
	}

	@Nullable
	static SkinTexture getAppliedSkin(Minecraft mc, UUID uuid) {
		var level = mc.level;

		if (level != null) {
			var player = level.getPlayerByUUID(uuid);

			if (player instanceof AbstractClientPlayer clientPlayer) {
				return ClientGameEngine.INSTANCE.overrideSkin(clientPlayer);
			}
		}

		return null;
	}

	@Nullable
	static PlayerSkin getLiveSkin(@Nullable Entity entity) {
		if (entity instanceof AbstractClientPlayer player) {
			return player.getSkin();
		}

		return null;
	}
}
