package dev.latvian.mods.vidlib.feature.gallery;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PlayerSkins {
	@ClientAutoRegister
	Gallery GALLERY = new Gallery("player_skins", () -> VidLibPaths.USER.get().resolve("player-skins"), TriState.TRUE);

	ResourceLocation DEFAULT_WIDE_SKIN_TEXTURE = ID.mc("textures/entity/player/wide/steve.png");
	ResourceLocation DEFAULT_SLIM_SKIN_TEXTURE = ID.mc("textures/entity/player/slim/steve.png");

	static GalleryImage get(Minecraft mc, UUID uuid) {
		return GALLERY.getRemote(mc, uuid, PlayerProfiles::getName, (id, n) -> PlayerProfiles.get(id).skinUrl().orElse(null), ImagePreProcessor.NONE);
	}

	static AbstractTexture getTexture(Minecraft mc, @Nullable UUID uuid) {
		if (uuid == null || uuid.equals(Util.NIL_UUID)) {
			return mc.getTextureManager().getTexture(DEFAULT_WIDE_SKIN_TEXTURE);
		}

		return get(mc, uuid).load(mc, false);
	}
}
