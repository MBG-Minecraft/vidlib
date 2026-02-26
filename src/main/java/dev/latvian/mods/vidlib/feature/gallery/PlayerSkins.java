package dev.latvian.mods.vidlib.feature.gallery;

import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfile;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PlayerSkins {
	@ClientAutoRegister
	Gallery<UUID> GALLERY = Gallery.ofUUIDKey("player_skins", () -> VidLibPaths.USER.get().resolve("player-skins"), TriState.TRUE);

	static PlayerSkin of(SkinTexture skin) {
		return new PlayerSkin(skin.texture(), null, null, null, skin.slim() ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE, true);
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
			return mc.getTextureManager().getTexture(DEFAULT_WIDE_SKINS[0].texture());
		}

		return get(mc, uuid).load(mc, false);
	}

	static PlayerSkin.Model getModelType(@Nullable PlayerProfile profile) {
		if (profile == null || profile.isError()) {
			return PlayerSkin.Model.WIDE;
		}

		return profile.slimModel() ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE;
	}

	static PlayerSkin getSkin(Minecraft mc, UUID uuid, boolean blocking) {
		if (uuid.equals(PlayerProfile.STEVE.profile().getId())) {
			return DEFAULT_WIDE_SKINS[0];
		} else if (uuid.equals(PlayerProfile.ALEX.profile().getId())) {
			return DEFAULT_SLIM_SKINS[1];
		} else {
			var profile = PlayerProfiles.get(uuid);
			var modelType = PlayerSkins.getModelType(profile);
			var skin = PlayerSkins.get(mc, uuid);
			skin.load(mc, blocking);
			return new PlayerSkin(skin.textureId(), null, null, null, modelType, true);
		}
	}
}
