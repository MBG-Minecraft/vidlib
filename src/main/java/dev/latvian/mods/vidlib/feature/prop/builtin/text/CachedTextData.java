package dev.latvian.mods.vidlib.feature.prop.builtin.text;

import net.minecraft.util.FormattedCharSequence;

public class CachedTextData {
	public final FormattedCharSequence[] lines;
	public final int[] width;
	public int totalWidth;

	public CachedTextData(FormattedCharSequence[] lines) {
		this.lines = lines;
		this.width = new int[lines.length];
		this.totalWidth = 0;
	}
}
