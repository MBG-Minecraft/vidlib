package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.util.ID;
import net.minecraft.core.ClientAsset;

public interface VidLibTextures {
	ClientAsset LOGO = new ClientAsset(ID.vidlib("misc/logo"));
	ClientAsset SQUARE = new ClientAsset(ID.mc("misc/white"));
	ClientAsset CIRCLE = new ClientAsset(ID.vidlib("misc/circle"));
	ClientAsset DEFAULT_MARKER = new ClientAsset(ID.vidlib("misc/default_marker"));
	ClientAsset DEFAULT_PLAYER_BODY = new ClientAsset(ID.vidlib("misc/default_player_body"));
	ClientAsset DEFAULT_PLAYER_HEAD = new ClientAsset(ID.vidlib("misc/default_player_head"));
	ClientAsset DEFAULT_PROJECT_ICON = new ClientAsset(ID.vidlib("misc/default_project_icon"));
	ClientAsset DITHER = new ClientAsset(ID.vidlib("misc/dither"));
	ClientAsset FOLDER = new ClientAsset(ID.vidlib("misc/folder"));
	ClientAsset ID_CARD = new ClientAsset(ID.vidlib("misc/id_card"));
	ClientAsset LOADING = new ClientAsset(ID.vidlib("misc/loading"));
	ClientAsset LOADING_SMALL = new ClientAsset(ID.vidlib("misc/loading_small"));
	ClientAsset MISSING = new ClientAsset(ID.vidlib("misc/missing"));
	ClientAsset NO = new ClientAsset(ID.vidlib("misc/no"));
	ClientAsset NO_OFF = new ClientAsset(ID.vidlib("misc/no_off"));
	ClientAsset NO_OUTLINE = new ClientAsset(ID.vidlib("misc/no_outline"));
	ClientAsset PACK = new ClientAsset(ID.vidlib("misc/pack"));
	ClientAsset TRANSPARENT = new ClientAsset(ID.vidlib("misc/transparent"));
	ClientAsset TRASH = new ClientAsset(ID.vidlib("misc/trash"));
	ClientAsset UNKNOWN_PACK = new ClientAsset(ID.vidlib("misc/unknown_pack"));
	ClientAsset YES = new ClientAsset(ID.vidlib("misc/yes"));
	ClientAsset YES_OFF = new ClientAsset(ID.vidlib("misc/yes_off"));
	ClientAsset YES_OUTLINE = new ClientAsset(ID.vidlib("misc/yes_outline"));
}
