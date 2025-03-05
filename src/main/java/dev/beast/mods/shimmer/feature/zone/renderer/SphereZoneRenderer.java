package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.SphereZoneShape;
import dev.beast.mods.shimmer.math.SpherePoints;
import dev.beast.mods.shimmer.math.SphereRenderer;
import dev.beast.mods.shimmer.util.ShimmerRenderTypes;

public class SphereZoneRenderer implements ZoneRenderer<SphereZoneShape> {
	public static final SphereZoneRenderer INSTANCE = new SphereZoneRenderer();

	@Override
	public void render(SphereZoneShape shape, Context ctx) {
		var ms = ctx.poseStack();
		ms.pushPose();
		ms.translate(shape.pos().x - ctx.cameraPos().x, shape.pos().y - ctx.cameraPos().y, shape.pos().z - ctx.cameraPos().z);
		float scale = (float) (shape.radius() * 2D);
		ms.scale(scale, scale, scale);
		SphereRenderer.renderDebugLines(SpherePoints.M, ms, ctx.buffers().getBuffer(ShimmerRenderTypes.DEBUG_LINES), ctx.outlineColor());
		SphereRenderer.renderDebugQuads(SpherePoints.M, ms, ctx.buffers().getBuffer(ShimmerRenderTypes.DEBUG_QUADS_NO_CULL), ctx.color());
		ms.popPose();
	}
}
