package dev.latvian.mods.vidlib.feature.icon.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.feature.icon.ItemIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

public interface ItemIconRenderer {
	static void draw(ItemIcon icon, Minecraft mc, GuiGraphicsExtractor graphics, int alpha) {
		graphics.fakeItem(icon.stack(), -8, -8);
	}

	static void render(ItemIcon icon, Minecraft mc, PoseStack ms, float delta, MultiBufferSource source, int light, int overlay) {
		ms.pushPose();
		ms.translate(0F, -0.25F, 0F);
		ms.scale(1.75F, 1.75F, 1.75F);
		var state = new ItemStackRenderState();
		mc.getItemModelResolver().updateForTopItem(state, icon.stack(), ItemDisplayContext.GROUND, mc.level, null, 0);
		var featureRenderDispatcher = mc.gameRenderer.getFeatureRenderDispatcher();
		state.submit(ms, featureRenderDispatcher.getSubmitNodeStorage(), light, OverlayTexture.NO_OVERLAY, 0);
		featureRenderDispatcher.renderAllFeatures();
		ms.popPose();
	}
}
