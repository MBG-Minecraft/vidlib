package dev.latvian.mods.vidlib.feature.screeneffect;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberVariables;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public abstract class ScreenEffectInstance {
	public int tick = 0;
	public int duration = 1;
	public KNumberVariables variables = KNumberVariables.EMPTY;

	public abstract ScreenEffectShaderType shaderType();

	public void snap() {
	}

	public void update(KNumberContext ctx) {
	}

	public abstract void upload(IntArrayList arr, float delta);

	public String getName() {
		return "Unknown Screen Effect";
	}

	public void imgui(ImGraphics graphics) {
	}
}
