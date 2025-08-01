package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public enum VidLibIcon {
	SMALL_SPACE(".", false),
	ERROR("!", true),
	PLUS("+", true),
	MINUS("-", true),
	TILDE("~", true),
	COPY("C", true),
	ELIMINATED("E", false),
	INFO("I", true),
	CAMERA("M", true),
	NO("N", true),
	PVP("P", false),
	SWORD("S", true),
	STAR("T", false),
	WARN("W", true),
	YES("Y", true),

	;

	public static final ResourceLocation FONT = VidLib.id("icons");

	public static Component icons(String text, boolean hasColor) {
		if (hasColor) {
			return Component.literal(text).withStyle(Style.EMPTY.withFont(FONT).withColor(ChatFormatting.WHITE));
		} else {
			return Component.literal(text).withStyle(Style.EMPTY.withFont(FONT));
		}
	}

	public final String character;
	public final boolean hasColor;

	VidLibIcon(String character, boolean hasColor) {
		this.character = character;
		this.hasColor = hasColor;
	}

	public Component icon() {
		return icons(character, hasColor);
	}

	public Component prefix() {
		return icons(character + ".", hasColor);
	}

	public Component suffix() {
		return icons("." + character, hasColor);
	}
}
