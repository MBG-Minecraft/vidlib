package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.ResourceLocation;

public interface VidLibTextures {
	ClientAsset LOGO = new ClientAsset(VidLib.id("misc/logo"));
	ClientAsset SQUARE = new ClientAsset(ResourceLocation.withDefaultNamespace("misc/white"));
	ClientAsset CIRCLE = new ClientAsset(VidLib.id("misc/circle"));
	ClientAsset DEFAULT_MARKER = new ClientAsset(VidLib.id("misc/default_marker"));
	ClientAsset DEFAULT_PLAYER_BODY = new ClientAsset(VidLib.id("misc/default_player_body"));
	ClientAsset DEFAULT_PLAYER_HEAD = new ClientAsset(VidLib.id("misc/default_player_head"));
	ClientAsset DITHER = new ClientAsset(VidLib.id("misc/dither"));
	ClientAsset FOLDER = new ClientAsset(VidLib.id("misc/folder"));
	ClientAsset ID_CARD = new ClientAsset(VidLib.id("misc/id_card"));
	ClientAsset LOADING = new ClientAsset(VidLib.id("misc/loading"));
	ClientAsset MISSING = new ClientAsset(VidLib.id("misc/missing"));
	ClientAsset NO = new ClientAsset(VidLib.id("misc/no"));
	ClientAsset NO_OFF = new ClientAsset(VidLib.id("misc/no_off"));
	ClientAsset NO_OUTLINE = new ClientAsset(VidLib.id("misc/no_outline"));
	ClientAsset TRANSPARENT = new ClientAsset(VidLib.id("misc/transparent"));
	ClientAsset TRASH = new ClientAsset(VidLib.id("misc/trash"));
	ClientAsset YES = new ClientAsset(VidLib.id("misc/yes"));
	ClientAsset YES_OFF = new ClientAsset(VidLib.id("misc/yes_off"));
	ClientAsset YES_OUTLINE = new ClientAsset(VidLib.id("misc/yes_outline"));
}
