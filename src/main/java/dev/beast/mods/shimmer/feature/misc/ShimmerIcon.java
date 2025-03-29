package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.Shimmer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public enum ShimmerIcon {
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

	public static final ResourceLocation FONT = Shimmer.id("icons");
	public static final String SMALL_SPACE = ".";

	public static Component icons(String text, boolean hasColor) {
		if (hasColor) {
			return Component.literal(text).withStyle(Style.EMPTY.withFont(FONT).withColor(ChatFormatting.WHITE));
		} else {
			return Component.literal(text).withStyle(Style.EMPTY.withFont(FONT));
		}
	}

	public final String character;
	public final boolean hasColor;

	ShimmerIcon(String character, boolean hasColor) {
		this.character = character;
		this.hasColor = hasColor;
	}

	public Component icon() {
		return icons(character, hasColor);
	}

	public Component prefix() {
		return icons(character + SMALL_SPACE, hasColor);
	}

	public Component suffix() {
		return icons(SMALL_SPACE + character, hasColor);
	}
}
