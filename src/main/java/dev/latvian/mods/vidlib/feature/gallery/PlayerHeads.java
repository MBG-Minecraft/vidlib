package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.util.UndashedUuid;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PlayerHeads {
	@ClientAutoRegister
	Gallery GALLERY = new Gallery("player_heads", () -> VidLibPaths.USER.get().resolve("player-heads"), TriState.TRUE);

	static GalleryImage get(Minecraft mc, UUID uuid) {
		// TODO: Render
		return GALLERY.getRemote(mc, uuid, PlayerProfiles::getName, (id, n) -> "https://cravatar.eu/helmhead/" + UndashedUuid.toString(id) + "/256.png", ImagePreProcessor.NONE);
	}

	static AbstractTexture getTexture(Minecraft mc, @Nullable UUID uuid) {
		if (uuid == null || uuid.equals(Util.NIL_UUID)) {
			return mc.getTextureManager().getTexture(VidLibTextures.DEFAULT_PLAYER_HEAD);
		}

		return get(mc, uuid).load(mc, false);
	}
}
