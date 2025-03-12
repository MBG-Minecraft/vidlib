package dev.beast.mods.shimmer.feature.icon.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.beast.mods.shimmer.feature.icon.TextureIcon;
import dev.beast.mods.shimmer.util.ShimmerRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3f;

public record TextureIconRenderer(TextureIcon icon) implements IconRenderer {
	@Override
	public void render3D(Minecraft mc, PoseStack ms, float delta, MultiBufferSource source, int light, int overlay) {
		var buffer = source.getBuffer(ShimmerRenderTypes.entityTextureCull(icon.texture(), icon.tint().alpha() < 255 || icon.translucent()));

		int colR = icon.tint().red();
		int colG = icon.tint().green();
		int colB = icon.tint().blue();
		int colA = icon.tint().alpha();

		var m = ms.last().pose();
		var n = ms.last().transformNormal(0F, 1F, 0F, new Vector3f());
		var uv = icon.uv();

		buffer.addVertex(m, -0.5F, -0.5F, 0F).setColor(colR, colG, colB, colA).setUv(uv.u0(), uv.v1()).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, 0.5F, -0.5F, 0F).setColor(colR, colG, colB, colA).setUv(uv.u1(), uv.v1()).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, 0.5F, 0.5F, 0F).setColor(colR, colG, colB, colA).setUv(uv.u1(), uv.v0()).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, -0.5F, 0.5F, 0F).setColor(colR, colG, colB, colA).setUv(uv.u0(), uv.v0()).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
	}
}
