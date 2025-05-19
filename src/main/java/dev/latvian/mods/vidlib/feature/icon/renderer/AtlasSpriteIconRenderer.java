package dev.latvian.mods.vidlib.feature.icon.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.feature.client.EntityRenderTypes;
import dev.latvian.mods.vidlib.feature.icon.AtlasSpriteIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.joml.Vector3f;

public record AtlasSpriteIconRenderer(AtlasSpriteIcon icon) implements IconRenderer {
	@Override
	public void render3D(Minecraft mc, PoseStack ms, float delta, MultiBufferSource source, int light, int overlay) {
		var texture = icon.atlas().orElse(TextureAtlas.LOCATION_BLOCKS);
		var buffer = source.getBuffer(EntityRenderTypes.textureCull(texture, icon.tint().alpha() < 255 || icon.translucent()));

		int colR = icon.tint().red();
		int colG = icon.tint().green();
		int colB = icon.tint().blue();
		int colA = icon.tint().alpha();

		var m = ms.last().pose();
		var n = ms.last().transformNormal(0F, 1F, 0F, new Vector3f());
		var uv = mc.getTextureAtlas(texture).apply(icon.sprite());

		buffer.addVertex(m, -0.5F, -0.5F, 0F).setColor(colR, colG, colB, colA).setUv(uv.getU0(), uv.getV1()).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, 0.5F, -0.5F, 0F).setColor(colR, colG, colB, colA).setUv(uv.getU1(), uv.getV1()).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, 0.5F, 0.5F, 0F).setColor(colR, colG, colB, colA).setUv(uv.getU1(), uv.getV0()).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, -0.5F, 0.5F, 0F).setColor(colR, colG, colB, colA).setUv(uv.getU0(), uv.getV0()).setLight(light).setOverlay(overlay).setNormal(n.x, n.y, n.z);
	}
}
