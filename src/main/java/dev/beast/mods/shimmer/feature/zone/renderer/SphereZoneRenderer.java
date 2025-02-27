package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.SphereZoneShape;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.math.SpherePoints;
import dev.beast.mods.shimmer.math.SphereRenderer;
import dev.beast.mods.shimmer.util.ShimmerRenderTypes;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class SphereZoneRenderer implements ZoneRenderer<SphereZoneShape> {
	public static final SphereZoneRenderer INSTANCE = new SphereZoneRenderer();

	@Override
	public void render(SphereZoneShape shape, Minecraft mc, RenderLevelStageEvent event, Color color, Color outlineColor) {
		var cameraPos = event.getCamera().getPosition();
		var ms = event.getPoseStack();
		ms.pushPose();
		ms.translate(shape.pos().x - cameraPos.x, shape.pos().y - cameraPos.y, shape.pos().z - cameraPos.z);
		float scale = (float) (shape.radius() * 2D);
		ms.scale(scale, scale, scale);
		SphereRenderer.renderDebugLines(SpherePoints.M, ms, mc.renderBuffers().bufferSource().getBuffer(ShimmerRenderTypes.DEBUG_LINES), outlineColor);
		SphereRenderer.renderDebugQuads(SpherePoints.M, ms, mc.renderBuffers().bufferSource().getBuffer(ShimmerRenderTypes.DEBUG_QUADS_NO_CULL), color);
		ms.popPose();
	}
}
