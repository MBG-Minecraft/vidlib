package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import net.minecraft.resources.Identifier;

import java.util.List;

public class ScreenEffectState {
	public ScreenEffect effect;
	public int tick = 0;
	public int duration = 1;
	public boolean paused = false;
	public String name = "Unknown";
	public KNumberVariables variables = null;
	public List<Identifier> postEffects = List.of();

	public void imgui(ImGraphics graphics) {
	}
}
