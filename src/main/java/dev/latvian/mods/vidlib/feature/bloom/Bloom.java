package dev.latvian.mods.vidlib.feature.bloom;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import imgui.type.ImBoolean;

public interface Bloom {
	@ClientAutoRegister
	Canvas CANVAS = Canvas.createExternal(VidLib.id("bloom"), builder -> {
	});

	ImBoolean VISIBLE = new ImBoolean(true);

	static void markActive() {
		if (VISIBLE.get()) {
			CANVAS.markActive();
		}
	}
}
