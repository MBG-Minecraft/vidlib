package dev.latvian.mods.vidlib.feature.decal;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.UniformSupplier;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import imgui.ImGui;
import imgui.type.ImBoolean;

import java.util.List;

public class DecalCanvas {
	public static final ImBoolean ENABLED = new ImBoolean(true);

	public static int count = 0;
	public static int irisCompat = 0;

	public static final Canvas CANVAS = new Canvas(
		ID.vidlib("decals"),
		() -> count > 0 && ENABLED.get(),
		"VidLibDecals",
		List.of(
			UniformSupplier.of("InverseViewProjectionMat", Canvas.inverseWorldMatrix),
			UniformSupplier.of("GameTimeSecond", () -> Canvas.gameTimeSecond),
			UniformSupplier.of("Count", () -> count),
			UniformSupplier.of("IrisCompat", () -> irisCompat)
		)
	);

	public static void imgui(ImGraphics graphics) {
		ImGui.checkbox("Enabled###enabled", ENABLED);
	}
}
