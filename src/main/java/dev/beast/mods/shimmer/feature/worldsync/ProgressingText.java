package dev.beast.mods.shimmer.feature.worldsync;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public class ProgressingText {
	public FormattedCharSequence text = FormattedCharSequence.EMPTY;
	public long progress = 0L;
	public long maxProgress = 0L;
	public int color = 0xFFFFFF;

	public ProgressingText setText(String text) {
		WorldSync.LOGGER.info(text);
		this.text = FormattedCharSequence.forward(text, Style.EMPTY);
		return this;
	}

	public ProgressingText red() {
		color = 0xFF3333;
		return this;
	}

	public ProgressingText green() {
		color = 0x33FF33;
		return this;
	}
}