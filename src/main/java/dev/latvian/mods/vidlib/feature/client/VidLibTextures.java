package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.util.ID;
import net.minecraft.core.ClientAsset;

public interface VidLibTextures {
	ClientAsset.ResourceTexture LOGO = new ClientAsset.ResourceTexture(ID.vidlib("misc/logo"));
	ClientAsset.ResourceTexture WHITE = new ClientAsset.ResourceTexture(ID.mc("misc/white"));
	ClientAsset.ResourceTexture CIRCLE = new ClientAsset.ResourceTexture(ID.vidlib("misc/circle"));
	ClientAsset.ResourceTexture DEFAULT_MARKER = new ClientAsset.ResourceTexture(ID.vidlib("misc/default_marker"));
	ClientAsset.ResourceTexture DEFAULT_PLAYER_BODY = new ClientAsset.ResourceTexture(ID.vidlib("misc/default_player_body"));
	ClientAsset.ResourceTexture DEFAULT_PLAYER_HEAD = new ClientAsset.ResourceTexture(ID.vidlib("misc/default_player_head"));
	ClientAsset.ResourceTexture DEFAULT_PROJECT_ICON = new ClientAsset.ResourceTexture(ID.vidlib("misc/default_project_icon"));
	ClientAsset.ResourceTexture DITHER = new ClientAsset.ResourceTexture(ID.vidlib("misc/dither"));
	ClientAsset.ResourceTexture FOLDER = new ClientAsset.ResourceTexture(ID.vidlib("misc/folder"));
	ClientAsset.ResourceTexture ID_CARD = new ClientAsset.ResourceTexture(ID.vidlib("misc/id_card"));
	ClientAsset.ResourceTexture LOADING = new ClientAsset.ResourceTexture(ID.vidlib("misc/loading"));
	ClientAsset.ResourceTexture LOADING_SMALL = new ClientAsset.ResourceTexture(ID.vidlib("misc/loading_small"));
	ClientAsset.ResourceTexture MISSING = new ClientAsset.ResourceTexture(ID.vidlib("misc/missing"));
	ClientAsset.ResourceTexture NO = new ClientAsset.ResourceTexture(ID.vidlib("misc/no"));
	ClientAsset.ResourceTexture NO_OFF = new ClientAsset.ResourceTexture(ID.vidlib("misc/no_off"));
	ClientAsset.ResourceTexture NO_OUTLINE = new ClientAsset.ResourceTexture(ID.vidlib("misc/no_outline"));
	ClientAsset.ResourceTexture PACK = new ClientAsset.ResourceTexture(ID.vidlib("misc/pack"));
	ClientAsset.ResourceTexture TRANSPARENT = new ClientAsset.ResourceTexture(ID.vidlib("misc/transparent"));
	ClientAsset.ResourceTexture TRASH = new ClientAsset.ResourceTexture(ID.vidlib("misc/trash"));
	ClientAsset.ResourceTexture UNKNOWN_PACK = new ClientAsset.ResourceTexture(ID.vidlib("misc/unknown_pack"));
	ClientAsset.ResourceTexture YES = new ClientAsset.ResourceTexture(ID.vidlib("misc/yes"));
	ClientAsset.ResourceTexture YES_OFF = new ClientAsset.ResourceTexture(ID.vidlib("misc/yes_off"));
	ClientAsset.ResourceTexture YES_OUTLINE = new ClientAsset.ResourceTexture(ID.vidlib("misc/yes_outline"));
}
