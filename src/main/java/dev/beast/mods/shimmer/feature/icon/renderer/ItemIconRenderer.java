package dev.beast.mods.shimmer.feature.icon.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.beast.mods.shimmer.feature.icon.ItemIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

public record ItemIconRenderer(ItemIcon icon) implements IconRenderer {
	@Override
	public void render3D(Minecraft mc, PoseStack ms, float delta, MultiBufferSource source, int light, int overlay) {
		ms.pushPose();
		ms.translate(0F, -0.25F, 0F);
		ms.scale(1.75F, 1.75F, 1.75F);
		mc.getItemRenderer().renderStatic(icon.stack(), ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, ms, source, mc.level, 0);
		ms.popPose();
	}
}
