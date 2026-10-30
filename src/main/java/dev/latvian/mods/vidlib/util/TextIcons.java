package dev.latvian.mods.vidlib.util;

import dev.latvian.mods.klib.util.ID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public interface TextIcons {
	Identifier FONT = ID.vidlib("icons");
	// 26.3 replaced Style#withFont(Identifier) with a FontDescription discriminator.
	FontDescription FONT_DESCRIPTION = new FontDescription.Resource(FONT);
	Style STYLE = Style.EMPTY.withFont(FONT_DESCRIPTION).applyFormat(ChatFormatting.WHITE);

	static MutableComponent icon(MutableComponent character) {
		return character.setStyle(STYLE);
	}

	static MutableComponent icons(String characters) {
		return Component.literal(characters).setStyle(STYLE);
	}
}
