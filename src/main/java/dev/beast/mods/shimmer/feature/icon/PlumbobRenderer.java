package dev.beast.mods.shimmer.feature.icon;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.beast.mods.shimmer.feature.icon.renderer.IconRenderer;
import dev.beast.mods.shimmer.util.FrameInfo;
import dev.latvian.mods.kmath.KMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

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

			render(mc, h, player.getEyePosition(pdelta), ms, frame.worldDelta(), source, light, player.isCrouching(), player.shimmer$sessionData().scoreText != null);
		}
	}

	public static void render(Minecraft mc, IconHolder icon, Vec3 pos, PoseStack ms, float delta, MultiBufferSource buffers, int light, boolean crouching, boolean scoreText) {
		var cam = mc.gameRenderer.getMainCamera().getPosition();

		if (KMath.sq(pos.x - cam.x) + KMath.sq(pos.z - cam.z) <= 0.01D * 0.01D) {
			return;
		}

		float y = 0.52F;

		if (scoreText) {
			y += 0.3F;
		}

		ms.pushPose();
		ms.translate(pos.x - cam.x, pos.y - cam.y + y, pos.z - cam.z);
		ms.translate(0F, y, 0F);
		ms.mulPose(mc.gameRenderer.getMainCamera().rotation());
		ms.scale(0.4F, 0.4F, 0.4F);

		if (icon.renderer == null) {
			icon.renderer = IconRenderer.create(icon.icon);
		}

		((IconRenderer) icon.renderer).render3D(mc, ms, delta, buffers, light, OverlayTexture.NO_OVERLAY);
		ms.popPose();
	}
}
