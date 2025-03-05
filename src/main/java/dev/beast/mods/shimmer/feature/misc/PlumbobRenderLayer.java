package dev.beast.mods.shimmer.feature.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.beast.mods.shimmer.math.KMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

public class PlumbobRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	public PlumbobRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack ms, MultiBufferSource source, int light, AbstractClientPlayer entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (!entity.isInvisible()) {
			var item = entity.get(InternalPlayerData.GLOBAL).plumbob;

			if (!item.isEmpty()) {
				var mc = Minecraft.getInstance();

				var cam = mc.gameRenderer.getMainCamera().getPosition();
				var pos = entity.getPosition(partialTick);

				if (KMath.sq(pos.x - cam.x) + KMath.sq(pos.z - cam.z) <= 0.01D * 0.01D) {
					return;
				}

				var model = mc.getItemRenderer().getModel(item, entity.level(), null, entity.getId());

				ms.pushPose();
				ms.scale(-1F, -1F, 1F);
				ms.translate(0F, entity.isCrouching() ? 0.5F : 0.8F, 0F);
				ms.mulPose(Axis.YP.rotationDegrees(entity.getPreciseBodyRotation(partialTick)));
				ms.mulPose(Axis.YP.rotation((float) (-Math.atan2(pos.z - cam.z, pos.x - cam.x) + Math.PI / 2D)));
				mc.getItemRenderer().render(item, ItemDisplayContext.GROUND, false, ms, source, light, OverlayTexture.NO_OVERLAY, model);
				ms.popPose();
			}
		}
	}
}
