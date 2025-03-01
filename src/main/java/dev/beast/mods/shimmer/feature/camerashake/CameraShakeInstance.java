package dev.beast.mods.shimmer.feature.camerashake;

public class CameraShakeInstance {
	public final CameraShake shake;
	public int ticks;

	public CameraShakeInstance(CameraShake shake) {
		this.shake = shake;
		this.ticks = 0;
	}
}
