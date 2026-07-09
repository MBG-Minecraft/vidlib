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
	Style STYLE = Style.EMPTY.withFont(new FontDescription.Resource(FONT)).applyFormat(ChatFormatting.WHITE);

	static MutableComponent icon(MutableComponent character) {
		return character.setStyle(STYLE);
	}

	static MutableComponent icons(String characters) {
		return Component.literal(characters).setStyle(STYLE);
	}
}
