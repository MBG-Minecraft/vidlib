package dev.latvian.mods.vidlib.feature.icon.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.feature.client.EntityRenderTypes;
import dev.latvian.mods.vidlib.feature.icon.TextureIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3f;

public interface TextureIconRenderer {
	static void draw(TextureIcon icon, Minecraft mc, GuiGraphicsExtractor graphics, int alpha) {
		var texture = mc.getTextureManager().getTexture(icon.texture().texturePath());
		var uv = icon.uv();
		graphics.blit(texture.getTextureView(), texture.getSampler(), -8, -8, 8, 8, uv.u0(), uv.u1(), uv.v0(), uv.v1());
	}

	static void render(TextureIcon icon, Minecraft mc, PoseStack ms, float delta, MultiBufferSource source, int light, int overlay) {
		var buffer = source.getBuffer(EntityRenderTypes.textureCull(icon.texture().texturePath(), icon.color().alpha() < 255 || icon.translucent()));

		int colR = icon.color().red();
		int colG = icon.color().green();
		int colB = icon.color().blue();
		int colA = icon.color().alpha();

		var m = ms.last().pose();
		var n = ms.last().transformNormal(0F, 1F, 0F, new Vector3f());
		var uv = icon.uv();

		buffer.addVertex(m, -0.5F, -0.5F, 0F).setColor(colR, colG, colB, colA).setUv(uv.u0(), uv.v1()).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, 0.5F, -0.5F, 0F).setColor(colR, colG, colB, colA).setUv(uv.u1(), uv.v1()).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, 0.5F, 0.5F, 0F).setColor(colR, colG, colB, colA).setUv(uv.u1(), uv.v0()).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, -0.5F, 0.5F, 0F).setColor(colR, colG, colB, colA).setUv(uv.u0(), uv.v0()).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
	}
}
