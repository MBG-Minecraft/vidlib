package dev.latvian.mods.vidlib.feature.camera;

public class ScreenShakeInstance {
	public final ScreenShake shake;
	public int ticks;
	public float progress;

	public ScreenShakeInstance(ScreenShake shake) {
		this.shake = shake;
		this.ticks = 0;
		this.progress = (float) (Math.random() * shake.speed());
	}
}
