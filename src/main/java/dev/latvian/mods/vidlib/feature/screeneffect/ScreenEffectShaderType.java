package dev.latvian.mods.vidlib.feature.screeneffect;

public enum ScreenEffectShaderType {
	COLOR(1),
	BLUR(2),
	ANGLED_BLUR(3),
	ZOOM_BLUR(4),
	DEPTH_OF_FIELD(5),
	FOCUSED_CHROMATIC_ABERRATION(6),
	ANGLED_CHROMATIC_ABERRATION(7),
	COLOR_BURN(8),
	RIPPLE(9), // https://www.shadertoy.com/view/ldBXDD
	SCREEN_SHAKE(10),
	CRT(11),
	UNUSED_12(12),
	UNUSED_13(13),
	UNUSED_14(14),
	UNUSED_15(15),

	;

	public final int shaderId;

	ScreenEffectShaderType(int shaderId) {
		this.shaderId = shaderId;
	}
}
