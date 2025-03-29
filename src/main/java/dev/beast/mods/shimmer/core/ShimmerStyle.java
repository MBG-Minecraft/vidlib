package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.math.Color;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public interface ShimmerStyle {
	default Style shimmer$self() {
		return (Style) this;
	}

	default Style withHoverText(Component component) {
		return shimmer$self().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component));
	}

	default Style withHoverText(String text) {
		return withHoverText(Component.literal(text));
	}

	default Style withClickToRunCommand(String string) {
		return shimmer$self().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, string));
	}

	default Style withClickToSuggestCommand(String string) {
		return shimmer$self().withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, string));
	}

	default Style withClickToCopyToClipboard(String string) {
		return shimmer$self().withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, string));
	}

	default Style withCopyString(String string) {
		return withClickToCopyToClipboard(string).withHoverText("Click to Copy");
	}

	default Style withColor(Color color) {
		return shimmer$self().withColor(color.rgb());
	}
}
