package dev.latvian.mods.vidlib.feature.gallery;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfile;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PlayerSkins {
	@ClientAutoRegister
	Gallery GALLERY = new Gallery("player_skins", () -> VidLibPaths.USER.get().resolve("player-skins"), TriState.TRUE);

	PlayerSkin[] DEFAULT_WIDE_SKINS = new PlayerSkin[]{
		new PlayerSkin(ID.mc("textures/entity/player/wide/steve.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/alex.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/ari.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/efe.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/kai.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/makena.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/noor.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/sunny.png"), null, null, null, PlayerSkin.Model.WIDE, true),
		new PlayerSkin(ID.mc("textures/entity/player/wide/zuri.png"), null, null, null, PlayerSkin.Model.WIDE, true)
	};

	PlayerSkin[] DEFAULT_SLIM_SKINS = new PlayerSkin[]{
		new PlayerSkin(ID.mc("textures/entity/player/slim/steve.png"), null, null, null, PlayerSkin.Model.SLIM, true),
		new PlayerSkin(ID.mc("textures/entity/player/slim/alex.png"), null, null, null, PlayerSkin.Model.SLIM, true),
		new PlayerSkin(ID.mc("textures/entity/player/slim/ari.png"), null, null, null, PlayerSkin.Model.SLIM, true),
		new PlayerSkin(ID.mc("textures/entity/player/slim/efe.png"), null, null, null, PlayerSkin.Model.SLIM, true),
		new PlayerSkin(ID.mc("textures/entity/player/slim/kai.png"), null, null, null, PlayerSkin.Model.SLIM, true),
		new PlayerSkin(ID.mc("textures/entity/player/slim/makena.png"), null, null, null, PlayerSkin.Model.SLIM, true),
		new PlayerSkin(ID.mc("textures/entity/player/slim/noor.png"), null, null, null, PlayerSkin.Model.SLIM, true),
		new PlayerSkin(ID.mc("textures/entity/player/slim/sunny.png"), null, null, null, PlayerSkin.Model.SLIM, true),
		new PlayerSkin(ID.mc("textures/entity/player/slim/zuri.png"), null, null, null, PlayerSkin.Model.SLIM, true)
	};

	static GalleryImage get(Minecraft mc, UUID uuid) {
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
