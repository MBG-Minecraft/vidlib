package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.kmath.texture.LightUV;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import dev.latvian.mods.vidlib.util.client.MultiBufferSourceOverride;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;

public class BossRendering {
	@AutoRegister(Dist.CLIENT)
	public static final Canvas CANVAS = Canvas.createExternal(VidLib.id("boss")).setDrawCallback(BossRendering::handleColors);

	public static boolean active = false;
	public static float lookingAtDepth = 1F;

	public static void handleColors() {
		lookingAtDepth = CANVAS.getCenterARGB() != 0 ? CANVAS.getCenterDepth() : 1F;
	}

	public static void render(FrameInfo frame) {
		var mc = frame.mc();
		var dispatcher = mc.getEntityRenderDispatcher();

		dispatcher.setRenderShadow(false);
		boolean hitbox = dispatcher.shouldRenderHitBoxes();
		dispatcher.setRenderHitBoxes(false);
		active = true;

		for (var entity : mc.level.getBosses()) {
			if (entity != mc.player) {
				try {
					var buffers = MultiBufferSourceOverride.boss(frame.buffers());
					float x = frame.x(Mth.lerp(frame.worldDelta(), entity.xOld, entity.getX()));
					float y = frame.y(Mth.lerp(frame.worldDelta(), entity.yOld, entity.getY()));
					float z = frame.z(Mth.lerp(frame.worldDelta(), entity.zOld, entity.getZ()));
					dispatcher.render(entity, x, y, z, frame.worldDelta(), frame.poseStack(), buffers, LightUV.FULLBRIGHT.light());
				} catch (Exception ignored) {
				}
			}
		}

		dispatcher.setRenderShadow(true);
		dispatcher.setRenderHitBoxes(hitbox);
		active = false;
	}
}
