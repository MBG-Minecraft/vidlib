package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;

public interface VidLibTextures {
	ClientAsset.ResourceTexture LOGO = new ClientAsset.ResourceTexture(VidLib.id("misc/logo"));
	ClientAsset.ResourceTexture SQUARE = new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("misc/white"));
	ClientAsset.ResourceTexture CIRCLE = new ClientAsset.ResourceTexture(VidLib.id("misc/circle"));
	ClientAsset.ResourceTexture DEFAULT_MARKER = new ClientAsset.ResourceTexture(VidLib.id("misc/default_marker"));
	ClientAsset.ResourceTexture DEFAULT_PLAYER_BODY = new ClientAsset.ResourceTexture(VidLib.id("misc/default_player_body"));
	ClientAsset.ResourceTexture DEFAULT_PLAYER_HEAD = new ClientAsset.ResourceTexture(VidLib.id("misc/default_player_head"));
	ClientAsset.ResourceTexture DITHER = new ClientAsset.ResourceTexture(VidLib.id("misc/dither"));
	ClientAsset.ResourceTexture FOLDER = new ClientAsset.ResourceTexture(VidLib.id("misc/folder"));
	ClientAsset.ResourceTexture ID_CARD = new ClientAsset.ResourceTexture(VidLib.id("misc/id_card"));
	ClientAsset.ResourceTexture LOADING = new ClientAsset.ResourceTexture(VidLib.id("misc/loading"));
	ClientAsset.ResourceTexture MISSING = new ClientAsset.ResourceTexture(VidLib.id("misc/missing"));
	ClientAsset.ResourceTexture NO = new ClientAsset.ResourceTexture(VidLib.id("misc/no"));
	ClientAsset.ResourceTexture NO_OFF = new ClientAsset.ResourceTexture(VidLib.id("misc/no_off"));
	ClientAsset.ResourceTexture NO_OUTLINE = new ClientAsset.ResourceTexture(VidLib.id("misc/no_outline"));
	ClientAsset.ResourceTexture TRANSPARENT = new ClientAsset.ResourceTexture(VidLib.id("misc/transparent"));
	ClientAsset.ResourceTexture TRASH = new ClientAsset.ResourceTexture(VidLib.id("misc/trash"));
	ClientAsset.ResourceTexture YES = new ClientAsset.ResourceTexture(VidLib.id("misc/yes"));
	ClientAsset.ResourceTexture YES_OFF = new ClientAsset.ResourceTexture(VidLib.id("misc/yes_off"));
	ClientAsset.ResourceTexture YES_OUTLINE = new ClientAsset.ResourceTexture(VidLib.id("misc/yes_outline"));
}
