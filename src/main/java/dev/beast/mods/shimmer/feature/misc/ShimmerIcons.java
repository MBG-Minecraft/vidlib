package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.Shimmer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public interface ShimmerIcons {
	ResourceLocation FONT = Shimmer.id("icons");
	String SMALL_SPACE = ".";
	String ERROR = "!";
	String PLUS = "+";
	String MINUS = "-";
	String TILDE = "~";
	String COPY = "C";
	String ELIMINATED = "E";
	String INFO = "I";
	String CAMERA = "M";
	String NO = "N";
	String PVP = "P";
	String SWORD = "S";
	String STAR = "T";
	String WARN = "W";
	String YES = "Y";

	static Component coloredIcons(String text) {
		return Component.literal(text).withStyle(Style.EMPTY.withFont(FONT).withColor(ChatFormatting.WHITE));
	}

	static Component icons(String text) {
		return Component.literal(text).withStyle(Style.EMPTY.withFont(FONT));
	}

	Component CAMERA_PREFIX = coloredIcons(CAMERA + SMALL_SPACE);
	Component STAFF_PREFIX = coloredIcons(SWORD + SMALL_SPACE);
	Component ELIMINATED_PREFIX = icons(ELIMINATED + SMALL_SPACE);
	Component STAR_PREFIX = icons(STAR + SMALL_SPACE);
	Component PVP_PREFIX = icons(PVP + SMALL_SPACE);
	Component WARN_PREFIX = coloredIcons(WARN + SMALL_SPACE);
}
