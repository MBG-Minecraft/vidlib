package dev.beast.mods.shimmer.feature.camerashake;

public class CameraShakeInstance {
	public final CameraShake shake;
	public int prevTicks;
	public int ticks;

	public CameraShakeInstance(CameraShake shake) {
		this.shake = shake;
		this.prevTicks = 0;
		this.ticks = 0;
	}
}
