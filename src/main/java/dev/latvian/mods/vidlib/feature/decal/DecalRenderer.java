package dev.latvian.mods.vidlib.feature.decal;

import dev.latvian.mods.klib.math.ClientMatrices;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.CanvasUniform;
import dev.latvian.mods.vidlib.integration.iris.IrisIntegration;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

public class DecalRenderer {
	private static int uCount = 0;
	private static float uGameTime = 0F;

	@ClientAutoRegister
	public static final Canvas CANVAS = Canvas.createExternal(ID.vidlib("decals"), builder -> {
		builder.setDrawSetupCallback(DecalRenderer::setup);
		builder.addUniform(CanvasUniform.int1("Count", () -> uCount));
		builder.addUniform(CanvasUniform.mat4("InverseViewProjectionMat", () -> ClientMatrices.INVERSE_WORLD));
		builder.addUniform(CanvasUniform.float1("GameTime", () -> uGameTime));
		builder.addUniform(CanvasUniform.int1("NoSceneSample", () -> IrisIntegration.INSTANCE.isShaderPackInUse() ? 1 : 0));
	});

	private static final List<Decal> TEMP_LIST = new ArrayList<>(1);

	public static void add(Decal decal) {
		decal.addToList(TEMP_LIST);
	}

	private static void setup(Minecraft mc) {
		uGameTime = mc.level == null ? 0F : mc.level.getGameTime() + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		var debugDecals = mc.player.vl$sessionData().debugDecals;

		if (!debugDecals.isEmpty()) {
			for (var decal : debugDecals) {
				add(decal);
			}
		}

		NeoForge.EVENT_BUS.post(new DecalEvent(TEMP_LIST));

		if (!TEMP_LIST.isEmpty()) {
			var texture = DecalTexture.HOLDER.texture().get();
			uCount = texture.update(TEMP_LIST, mc.gameRenderer.getMainCamera().position());
			CANVAS.markActive();
			TEMP_LIST.clear();
		}
	}
}
