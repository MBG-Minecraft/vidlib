package dev.latvian.mods.vidlib.util;

import dev.latvian.mods.klib.util.ID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public interface TextIcons {
	ResourceLocation FONT = ID.vidlib("icons");
	Style STYLE = Style.EMPTY.withFont(FONT).applyFormat(ChatFormatting.WHITE);

	static MutableComponent icon(MutableComponent character) {
		return character.setStyle(STYLE);
	}

	static MutableComponent icons(String characters) {
		return Component.literal(characters).setStyle(STYLE);
	}
}
