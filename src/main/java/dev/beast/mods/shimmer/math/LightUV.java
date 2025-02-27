package dev.beast.mods.shimmer.math;

public class LightUV {
	public static final LightUV NORMAL = new LightUV(0xF00000, 0xA0000);
	public static final LightUV FULLBRIGHT = new LightUV(0xF000F0, 0xA0000);
	public static final LightUV NORMAL_HURT = new LightUV(0xF00000, 0x30000);
	public static final LightUV FULLBRIGHT_HURT = new LightUV(0xF000F0, 0x30000);

	public static LightUV get(boolean fullbright, boolean hurt) {
		return fullbright ? (hurt ? FULLBRIGHT_HURT : FULLBRIGHT) : (hurt ? NORMAL_HURT : NORMAL);
	}

	public final int light;
	public final int overlay;
	public final int lightU;
	public final int lightV;
	public final int overlayU;
	public final int overlayV;

	public LightUV(int light, int overlay) {
		this.light = light;
		this.overlay = overlay;
		this.lightU = light & '\uffff';
		this.lightV = light >> 16 & '\uffff';
		this.overlayU = overlay & '\uffff';
		this.overlayV = overlay >> 16 & '\uffff';
	}
}
