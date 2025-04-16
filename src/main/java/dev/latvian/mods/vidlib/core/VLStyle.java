package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.kmath.color.Color;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public interface VLStyle {
	default Style vl$self() {
		return (Style) this;
	}

	default Style withHoverText(Component component) {
		return vl$self().withHoverEvent(new HoverEvent.ShowText(component));
	}

	default Style withHoverText(String text) {
		return withHoverText(Component.literal(text));
	}

	default Style withClickToRunCommand(String string) {
		return vl$self().withClickEvent(new ClickEvent.RunCommand(string));
	}

	default Style withClickToSuggestCommand(String string) {
		return vl$self().withClickEvent(new ClickEvent.SuggestCommand(string));
	}

	default Style withClickToCopyToClipboard(String string) {
		return vl$self().withClickEvent(new ClickEvent.CopyToClipboard(string));
	}

	default Style withCopyString(String string) {
		return withClickToCopyToClipboard(string).withHoverText("Click to Copy");
	}

	default Style withColor(Color color) {
		return vl$self().withColor(color.rgb());
	}
}
