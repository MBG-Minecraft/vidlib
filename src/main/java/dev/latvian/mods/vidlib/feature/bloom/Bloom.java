package dev.latvian.mods.vidlib.feature.bloom;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import net.neoforged.api.distmarker.Dist;

public class Bloom {
	@AutoRegister(Dist.CLIENT)
	public static final Canvas CANVAS = Canvas.createExternal(VidLib.id("bloom"));
}
