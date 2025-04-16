package dev.latvian.mods.vidlib.feature.camera;

public class CameraShakeInstance {
	public final CameraShake shake;
	public int ticks;
	public float progress;

	public CameraShakeInstance(CameraShake shake) {
		this.shake = shake;
		this.ticks = 0;
		this.progress = (float) (Math.random() * shake.speed());
	}
}
