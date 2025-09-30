package dev.latvian.mods.vidlib.feature.decal;

import dev.latvian.mods.klib.math.ClientMatrices;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.CanvasFloatUniform;
import dev.latvian.mods.vidlib.feature.canvas.CanvasIntUniform;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class DecalRenderer {
	@ClientAutoRegister
	public static final Canvas CANVAS = Canvas.createExternal(VidLib.id("decals")).setDrawSetupCallback(DecalRenderer::setup);

	// public static final CanvasSampler TEXTURE = CANVAS.sampler("DecalsSampler");
	public static final CanvasIntUniform COUNT = CANVAS.intUniform("DecalCount");
	public static final CanvasFloatUniform INVERSE_VIEW_PROJECTION_MATRIX_UNIFORM = CANVAS.mat4Uniform("InverseViewProjectionMat");

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
			var texture = mc.getTextureManager().byPath.get(DecalTexture.ID);

			if (texture == null) {
				texture = new DecalTexture();
				mc.getTextureManager().register(DecalTexture.ID, texture);
			}

			((DecalTexture) texture).update(TEMP_LIST, mc.gameRenderer.getMainCamera().getPosition());
			// TEXTURE.set(texture.getTexture());
			COUNT.set(TEMP_LIST.size());
			INVERSE_VIEW_PROJECTION_MATRIX_UNIFORM.set(ClientMatrices.INVERSE_WORLD);
			CANVAS.markActive();
			TEMP_LIST.clear();
		}
	}
}
