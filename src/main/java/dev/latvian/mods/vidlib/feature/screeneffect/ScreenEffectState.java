package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;

public class ScreenEffectState {
	public ScreenEffect effect;
	public int tick = 0;
	public int duration = 1;
	public boolean paused = false;
	public String name = "Unknown";
	public KNumberVariables variables = null;

	public void imgui(ImGraphics graphics) {
	}
}
