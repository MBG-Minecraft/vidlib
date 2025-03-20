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
	String INFO = "I";
	String CAMERA = "M";
	String NO = "N";
	String SWORD = "S";
	String WARN = "W";
	String YES = "Y";

	static Component icons(String text) {
		return Component.literal(text).withStyle(Style.EMPTY.withFont(FONT).withColor(ChatFormatting.WHITE));
	}

	Component CAMERA_PREFIX = icons(CAMERA + SMALL_SPACE);
	Component STAFF_PREFIX = icons(SWORD + SMALL_SPACE);
	Component ELIMINATED_PREFIX = icons("☠" + SMALL_SPACE);
}
