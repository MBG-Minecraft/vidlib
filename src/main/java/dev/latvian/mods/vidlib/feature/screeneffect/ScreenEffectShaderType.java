package dev.latvian.mods.vidlib.feature.screeneffect;

public enum ScreenEffectShaderType {
	COLOR(1),
	FOCUSED_CHROMATIC_ABERRATION(2),
	ANGLED_CHROMATIC_ABERRATION(3),

	;

	public final int shaderId;

	ScreenEffectShaderType(int shaderId) {
		this.shaderId = shaderId;
	}
}
