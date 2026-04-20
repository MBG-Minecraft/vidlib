package dev.latvian.mods.vidlib.feature.icon.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.feature.client.EntityRenderTypes;
import dev.latvian.mods.vidlib.feature.icon.ColorIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3f;

public interface ColorIconRenderer {
	static void draw(ColorIcon icon, Minecraft mc, GuiGraphics graphics, int alpha) {
		graphics.fill(-8, -8, 8, 8, icon.color().mixAlpha(alpha).argb());
	}

	static void render(ColorIcon icon, Minecraft mc, PoseStack ms, float delta, MultiBufferSource source, int light, int overlay) {
		var buffer = source.getBuffer(icon.color().alpha() < 255 ? EntityRenderTypes.WHITE_TRANSLUCENT : EntityRenderTypes.WHITE);

		int colR = icon.color().red();
		int colG = icon.color().green();
		int colB = icon.color().blue();
		int colA = icon.color().alpha();

		var m = ms.last().pose();
		var n = ms.last().transformNormal(0F, 1F, 0F, new Vector3f());

		buffer.addVertex(m, -0.5F, -0.5F, 0F).setColor(colR, colG, colB, colA).setUv(0F, 1F).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, 0.5F, -0.5F, 0F).setColor(colR, colG, colB, colA).setUv(1F, 1F).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, 0.5F, 0.5F, 0F).setColor(colR, colG, colB, colA).setUv(1F, 0F).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, -0.5F, 0.5F, 0F).setColor(colR, colG, colB, colA).setUv(0F, 0F).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
	}
}
