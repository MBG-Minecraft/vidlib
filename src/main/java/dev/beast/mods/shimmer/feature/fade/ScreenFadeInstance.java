package dev.beast.mods.shimmer.feature.fade;

public class ScreenFadeInstance {
	public final Fade data;
	public final int totalTicks;
	public float prevAlpha, alpha;
	public int tick;

	public ScreenFadeInstance(Fade data) {
		this.data = data;
		this.totalTicks = data.fadeInTicks() + data.pauseTicks() + data.fadeOutTicks();
	}

	public boolean tick() {
		prevAlpha = alpha;

		if (tick < data.fadeInTicks()) {
			alpha = data.fadeInEase().ease(tick / (float) data.fadeInTicks());
		} else if (tick < data.fadeInTicks() + data.pauseTicks()) {
			alpha = 1F;
		} else if (tick < totalTicks) {
			alpha = 1F - data.fadeOutEase().ease((tick - data.fadeInTicks() - data.pauseTicks()) / (float) data.fadeOutTicks());
		} else {
			alpha = 0F;
		}

		return ++tick >= totalTicks;
	}
}
