package dev.beast.mods.shimmer.feature.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.beast.mods.shimmer.feature.icon.renderer.IconRenderer;
import dev.beast.mods.shimmer.math.KMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class PlumbobRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	public PlumbobRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack ms, MultiBufferSource source, int light, AbstractClientPlayer entity, float limbSwing, float limbSwingAmount, float delta, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInvisible()) {
			return;
		}

		var h = entity.shimmer$sessionData().plumbobIcon;

		if (h == null) {
			return;
		}

		var mc = Minecraft.getInstance();

		var cam = mc.gameRenderer.getMainCamera().getPosition();
		var pos = entity.getPosition(delta);

		if (KMath.sq(pos.x - cam.x) + KMath.sq(pos.z - cam.z) <= 0.01D * 0.01D) {
			return;
		}

		ms.pushPose();
		ms.scale(-1F, -1F, 1F);
		ms.translate(0F, entity.isCrouching() ? 1.0F : 1.3F, 0F);
		ms.mulPose(Axis.YP.rotationDegrees(entity.getPreciseBodyRotation(delta)));
		ms.mulPose(Axis.YP.rotation((float) (-Math.atan2(pos.z - cam.z, pos.x - cam.x) + Math.PI / 2D)));
		ms.scale(0.4F, 0.4F, 0.4F);

		if (h.renderer == null) {
			h.renderer = IconRenderer.create(h.icon);
		}

		((IconRenderer) h.renderer).render3D(mc, ms, delta, source, light, OverlayTexture.NO_OVERLAY);
		ms.popPose();
	}
}
