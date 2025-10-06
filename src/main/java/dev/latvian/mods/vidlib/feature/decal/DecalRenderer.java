package dev.latvian.mods.vidlib.feature.decal;

import dev.latvian.mods.klib.math.ClientMatrices;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.CanvasUniform;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class DecalRenderer {
	private static int uCount = 0;

	@ClientAutoRegister
	public static final Canvas CANVAS = Canvas.createExternal(VidLib.id("decals"), builder -> {
		builder.setDrawSetupCallback(DecalRenderer::setup);
		builder.addUniform(CanvasUniform.int1("Count", () -> uCount));
		builder.addUniform(CanvasUniform.mat4("InverseViewProjectionMat", () -> ClientMatrices.INVERSE_WORLD));
	});

	private static final List<Decal> TEMP_LIST = new ArrayList<>();

	public static void add(Decal decal) {
		decal.addToList(TEMP_LIST);
	}

	private static void setup(Minecraft mc) {
		var debugDecals = mc.player.vl$sessionData().debugDecals;

		if (!debugDecals.isEmpty()) {
			for (var decal : debugDecals) {
				add(decal);
			}
		}

		if (!TEMP_LIST.isEmpty()) {
			var texture = DecalTexture.HOLDER.texture().get();
			uCount = texture.update(TEMP_LIST, mc.gameRenderer.getMainCamera().getPosition());
			CANVAS.markActive();
			TEMP_LIST.clear();
		}
	}
}
