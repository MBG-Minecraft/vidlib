package dev.latvian.mods.vidlib.feature.icon.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.feature.client.EntityRenderTypes;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.icon.TextureIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3f;

public interface TextureIconRenderer {
	static void draw(TextureIcon icon, Minecraft mc, GuiGraphics graphics, int alpha) {
		var rendertype = VidLibRenderTypes.GUI.apply(icon.texture());
		var matrix4f = graphics.pose().last().pose();
		var buffer = graphics.vl$buffers().getBuffer(rendertype);
		var color = icon.color().mixAlpha(alpha).argb();
		var uv = icon.uv();
		buffer.addVertex(matrix4f, -8F, -8F, 0F).setUv(uv.u0(), uv.v0()).setColor(color);
		buffer.addVertex(matrix4f, -8F, 8F, 0F).setUv(uv.u0(), uv.v1()).setColor(color);
		buffer.addVertex(matrix4f, 8F, 8F, 0F).setUv(uv.u1(), uv.v1()).setColor(color);
		buffer.addVertex(matrix4f, 8F, -8F, 0F).setUv(uv.u1(), uv.v0()).setColor(color);
	}

	static void render(TextureIcon icon, Minecraft mc, PoseStack ms, float delta, MultiBufferSource source, int light, int overlay) {
		var buffer = source.getBuffer(EntityRenderTypes.textureCull(icon.texture(), icon.color().alpha() < 255 || icon.translucent()));

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
