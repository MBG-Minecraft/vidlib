package dev.latvian.mods.vidlib.util;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;

import java.util.ArrayList;
import java.util.List;

public class FormattedCharSinkPartBuilder implements FormattedCharSink {
	public record Part(String text, Style style) {
	}

	private final List<Part> parts;
	private final StringBuilder builder;
	private Style style;

	public FormattedCharSinkPartBuilder() {
		this.parts = new ArrayList<>(1);
		this.builder = new StringBuilder();
		this.style = Style.EMPTY;
	}

	@Override
	public boolean accept(int pos, Style s, int codePoint) {
		if (!style.equals(s)) {
			if (!builder.isEmpty()) {
				parts.add(new Part(builder.toString(), style));
				builder.setLength(0);
			}
		}

		style = s;
		builder.append((char) codePoint);
		return true;
	}

	public List<Part> build() {
		if (!builder.isEmpty()) {
			parts.add(new Part(builder.toString(), style));
		}

		var out = List.copyOf(parts);
		parts.clear();
		builder.setLength(0);
		style = Style.EMPTY;
		return out;
	}
}
