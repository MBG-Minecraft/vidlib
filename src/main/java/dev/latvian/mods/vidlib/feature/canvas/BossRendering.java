package dev.latvian.mods.vidlib.feature.canvas;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;

public class BossRendering {
	@AutoRegister(Dist.CLIENT)
	public static final Canvas CANVAS = Canvas.createExternal(VidLib.id("boss")).setDrawCallback(BossRendering::handleColors);

	public static int active = 0;
	public static float lookingAtDepth = 1F;
	public static float lookingAtDistance = 8192F;

	public static void handleColors(Minecraft mc) {
		lookingAtDepth = CANVAS.getCenterARGB() != 0 ? CANVAS.getCenterDepth() : 1F;
		lookingAtDistance = mc.linearizeDepth(lookingAtDepth);
	}

	public static void render(FrameInfo frame) {
		var mc = frame.mc();
		var buffers = frame.buffers();
		var delta = frame.worldDelta();
		var entity = mc.level.getMainBoss();

		if (entity == null) {
			return;
		}

		var dispatcher = mc.getEntityRenderDispatcher();
		dispatcher.setRenderShadow(false);
		boolean hitbox = dispatcher.shouldRenderHitBoxes();
		active++;
		frame.poseStack().pushPose();

		try {
			var renderer = dispatcher.getRenderer(entity);

			if (renderer != null && renderer.shouldRender(entity, frame.frustum(), frame.cameraX(), frame.cameraY(), frame.cameraZ())) {
				float x = frame.x(Mth.lerp(delta, entity.xOld, entity.getX()));
				float y = frame.y(Mth.lerp(delta, entity.yOld, entity.getY()));
				float z = frame.z(Mth.lerp(delta, entity.zOld, entity.getZ()));
				renderer.renderBoss(entity, frame.poseStack(), buffers, x, y, z, delta);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		frame.poseStack().popPose();
		dispatcher.setRenderShadow(true);
		dispatcher.setRenderHitBoxes(hitbox);
		active--;
	}
}