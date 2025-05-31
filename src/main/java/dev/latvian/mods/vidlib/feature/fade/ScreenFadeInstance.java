package dev.latvian.mods.vidlib.feature.fade;

import dev.latvian.mods.kmath.color.Gradient;
import dev.latvian.mods.kmath.easing.Easing;

public class ScreenFadeInstance {
	public final Gradient color;
	public final int fadeInTicks;
	public final int pauseTicks;
	public final int fadeOutTicks;
	public final Easing fadeInEase;
	public final Easing fadeOutEase;
	public final int totalTicks;
	public float prevAlpha, alpha;
	public int prevTick, tick;

	public ScreenFadeInstance(Fade data) {
		this.color = data.color().resolve();
		this.fadeInTicks = data.fadeInTicks();
		this.pauseTicks = data.pauseTicks();
		this.fadeOutTicks = data.fadeOutTicks().orElse(data.fadeInTicks());
		this.fadeInEase = data.fadeInEase();
		this.fadeOutEase = data.fadeOutEase().orElse(data.fadeInEase());
		this.totalTicks = fadeInTicks + pauseTicks + fadeOutTicks;
	}

	public boolean tick() {
		prevTick = tick;
		prevAlpha = alpha;

		if (tick < fadeInTicks) {
			alpha = fadeInEase.ease(tick / (float) fadeInTicks);
		} else if (tick < fadeInTicks + pauseTicks) {
			alpha = 1F;
		} else if (tick < totalTicks) {
			alpha = 1F - fadeOutEase.ease((tick - fadeInTicks - pauseTicks) / (float) fadeOutTicks);
		} else {
			alpha = 0F;
		}

		return ++tick >= totalTicks;
	}
}
