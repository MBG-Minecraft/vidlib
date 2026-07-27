package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.util.ID;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;

public interface VidLibTextures {
	ClientAsset.ResourceTexture LOGO = new ClientAsset.ResourceTexture(ID.vidlib("misc/logo"));
	ClientAsset.ResourceTexture SQUARE = new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("misc/white"));
	ClientAsset.ResourceTexture CIRCLE = new ClientAsset.ResourceTexture(ID.vidlib("misc/circle"));
	ClientAsset.ResourceTexture DEFAULT_MARKER = new ClientAsset.ResourceTexture(ID.vidlib("misc/default_marker"));
	ClientAsset.ResourceTexture DEFAULT_PLAYER_BODY = new ClientAsset.ResourceTexture(ID.vidlib("misc/default_player_body"));
	ClientAsset.ResourceTexture DEFAULT_PLAYER_HEAD = new ClientAsset.ResourceTexture(ID.vidlib("misc/default_player_head"));
	ClientAsset.ResourceTexture DITHER = new ClientAsset.ResourceTexture(ID.vidlib("misc/dither"));
	ClientAsset.ResourceTexture FOLDER = new ClientAsset.ResourceTexture(ID.vidlib("misc/folder"));
	ClientAsset.ResourceTexture ID_CARD = new ClientAsset.ResourceTexture(ID.vidlib("misc/id_card"));
	ClientAsset.ResourceTexture LOADING = new ClientAsset.ResourceTexture(ID.vidlib("misc/loading"));
	ClientAsset.ResourceTexture MISSING = new ClientAsset.ResourceTexture(ID.vidlib("misc/missing"));
	ClientAsset.ResourceTexture NO = new ClientAsset.ResourceTexture(ID.vidlib("misc/no"));
	ClientAsset.ResourceTexture NO_OFF = new ClientAsset.ResourceTexture(ID.vidlib("misc/no_off"));
	ClientAsset.ResourceTexture NO_OUTLINE = new ClientAsset.ResourceTexture(ID.vidlib("misc/no_outline"));
	ClientAsset.ResourceTexture TRANSPARENT = new ClientAsset.ResourceTexture(ID.vidlib("misc/transparent"));
	ClientAsset.ResourceTexture TRASH = new ClientAsset.ResourceTexture(ID.vidlib("misc/trash"));
	ClientAsset.ResourceTexture YES = new ClientAsset.ResourceTexture(ID.vidlib("misc/yes"));
	ClientAsset.ResourceTexture YES_OFF = new ClientAsset.ResourceTexture(ID.vidlib("misc/yes_off"));
	ClientAsset.ResourceTexture YES_OUTLINE = new ClientAsset.ResourceTexture(ID.vidlib("misc/yes_outline"));
}
