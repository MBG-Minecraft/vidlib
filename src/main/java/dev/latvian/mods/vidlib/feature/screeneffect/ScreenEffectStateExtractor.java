package dev.latvian.mods.vidlib.feature.screeneffect;

@FunctionalInterface
public interface ScreenEffectStateExtractor<SE extends ScreenEffect, S extends ScreenEffectState> {
	void extract(S state, SE effect, ScreenEffectExtractContext ctx);
}
