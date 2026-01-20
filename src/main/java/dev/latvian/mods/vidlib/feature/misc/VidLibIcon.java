package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public interface VidLibIcon {
	Style STYLE_RGB = Style.EMPTY.withFont(VidLib.id("icons"));
	Style STYLE = STYLE_RGB.withColor(Color.WHITE);

	private static Component icon(char c, boolean rgb) {
		return Component.literal(String.valueOf(c)).setStyle(rgb ? STYLE_RGB : STYLE);
	}

	Component STAR = icon('*', true);
	Component ELIMINATED = icon('E', true);
	Component PVP = icon('V', true);
}
