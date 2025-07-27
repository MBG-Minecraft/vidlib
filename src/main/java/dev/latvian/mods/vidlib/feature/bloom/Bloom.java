package dev.latvian.mods.vidlib.feature.bloom;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import imgui.type.ImBoolean;
import net.neoforged.api.distmarker.Dist;

public interface Bloom {
	@AutoRegister(Dist.CLIENT)
	Canvas CANVAS = Canvas.createExternal(VidLib.id("bloom"));

	ImBoolean VISIBLE = new ImBoolean(true);

	static void markActive() {
		if (VISIBLE.get()) {
			CANVAS.markActive();
		}
	}
}
