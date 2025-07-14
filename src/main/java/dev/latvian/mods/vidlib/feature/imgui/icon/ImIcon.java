package dev.latvian.mods.vidlib.feature.imgui.icon;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;

public interface ImIcon {
	ImIcon NONE = new ImIcon() {
		@Override
		public String iconName() {
			return "NONE";
		}

		@Override
		public char toChar() {
			return 0;
		}

		@Override
		public String toString() {
			return "";
		}

		@Override
		public String formatLabel(ImGraphics graphics, String label) {
			return label;
		}
	};

	default String iconName() {
		return toString();
	}

	char toChar();

	default String formatLabel(ImGraphics graphics, String label) {
		if (label.isEmpty()) {
			return toString();
		}

		@SuppressWarnings("StringBufferReplaceableByString")
		var builder = new StringBuilder(2 + label.length());
		builder.append(toChar());
		builder.append(' ');
		builder.append(label);
		return builder.toString();
	}
}
