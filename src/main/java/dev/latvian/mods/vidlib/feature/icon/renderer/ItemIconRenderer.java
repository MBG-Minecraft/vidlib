package dev.latvian.mods.vidlib.feature.icon.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.feature.icon.ItemIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

public interface ItemIconRenderer {
	static void draw(ItemIcon icon, Minecraft mc, GuiGraphics graphics, int alpha) {
		graphics.renderFakeItem(icon.stack(), -8, -8);
	}

	static void render(ItemIcon icon, Minecraft mc, PoseStack ms, float delta, MultiBufferSource source, int light, int overlay) {
		ms.pushPose();
		ms.translate(0F, -0.25F, 0F);
		ms.scale(1.75F, 1.75F, 1.75F);
		mc.getItemRenderer().renderStatic(icon.stack(), ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, ms, source, mc.level, 0);
		ms.popPose();
	}
}
