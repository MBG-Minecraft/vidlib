package dev.latvian.mods.vidlib.feature.icon.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.icon.ColorIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3f;

public record ColorIconRenderer(ColorIcon icon) implements IconRenderer {
	@Override
	public void render3D(Minecraft mc, PoseStack ms, float delta, MultiBufferSource source, int light, int overlay) {
		var buffer = source.getBuffer(icon.color().alpha() < 255 ? VidLibRenderTypes.Entity.WHITE_TRANSLUCENT : VidLibRenderTypes.Entity.WHITE);

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
