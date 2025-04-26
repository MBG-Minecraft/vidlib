package dev.beast.mods.shimmer.feature.icon;

import dev.beast.mods.shimmer.feature.icon.renderer.IconRenderer;
import dev.beast.mods.shimmer.util.FrameInfo;
import dev.latvian.mods.kmath.KMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

public class PlumbobRenderer {
	public static void render(Minecraft mc, FrameInfo frame) {
		var ms = frame.poseStack();

		for (var player : mc.level.players()) {
			if (player.isInvisible()) {
				continue;
			}

			var h = player.getPlumbobHolder();

			if (h == null || player == mc.player && mc.options.getCameraType().isFirstPerson()) {
				continue;
			}

			float pdelta = player == mc.player ? frame.screenDelta() : frame.worldDelta();

			var source = mc.renderBuffers().bufferSource();
			var blockpos = BlockPos.containing(player.getLightProbePosition(pdelta));
			int light = LightTexture.pack(mc.level.getBrightness(LightLayer.BLOCK, blockpos), mc.level.getBrightness(LightLayer.SKY, blockpos));

			var cam = mc.gameRenderer.getMainCamera().getPosition();
			var pos = player.getPosition(pdelta);

			if (KMath.sq(pos.x - cam.x) + KMath.sq(pos.z - cam.z) <= 0.01D * 0.01D) {
				continue;
			}

			float y = 2.6F;

			if (player.isCrouching()) {
				y -= 0.4F;
			}

			if (player.shimmer$sessionData().scoreText != null) {
				y += 0.3F;
			}

			ms.pushPose();
			frame.translate(pos);
			ms.translate(0F, y, 0F);
			ms.mulPose(mc.gameRenderer.getMainCamera().rotation());
			ms.scale(0.4F, 0.4F, 0.4F);

			if (h.renderer == null) {
				h.renderer = IconRenderer.create(h.icon);
			}

			((IconRenderer) h.renderer).render3D(mc, ms, frame.worldDelta(), source, light, OverlayTexture.NO_OVERLAY);
			ms.popPose();
		}
	}
}
