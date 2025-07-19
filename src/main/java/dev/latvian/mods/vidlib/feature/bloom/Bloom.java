package dev.latvian.mods.vidlib.feature.bloom;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import net.neoforged.api.distmarker.Dist;

public interface Bloom {
	@AutoRegister(Dist.CLIENT)
	Canvas CANVAS = Canvas.createExternal(VidLib.id("bloom"));

	static void markActive() {
		CANVAS.markActive();
	}
}
