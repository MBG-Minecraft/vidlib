package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executor;

public interface PlayerSkins {
	ResourceLocation DEFAULT_SKIN_TEXTURE = ID.mc("textures/entity/player/wide/steve.png");
	Gallery GALLERY = new Gallery("player_skins", () -> VidLibPaths.USER.get().resolve("player-skins"), TriState.TRUE);

	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	static void reload(TextureManager manager, Executor backgroundExecutor, Executor gameExecutor) throws IOException {
		GALLERY.load(manager);
	}

	static GalleryImage get(Minecraft mc, UUID uuid, String name) {
		return GALLERY.getRemote(mc, uuid, name, id -> "https://crafthead.net/skin/" + UndashedUuid.toString(id), ImagePreProcessor.NONE);
	}

	static AbstractTexture getTexture(Minecraft mc, @Nullable UUID uuid, String name) {
		if (uuid == null || uuid.equals(Util.NIL_UUID)) {
			return mc.getTextureManager().getTexture(DEFAULT_SKIN_TEXTURE);
		}

		return get(mc, uuid, name).load(mc);
	}
}
