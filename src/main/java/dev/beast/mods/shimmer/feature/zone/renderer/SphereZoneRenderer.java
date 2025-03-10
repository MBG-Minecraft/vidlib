package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.shape.SphereZoneShape;
import dev.beast.mods.shimmer.math.BoxRenderer;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.math.SpherePoints;
import dev.beast.mods.shimmer.math.SphereRenderer;

public class SphereZoneRenderer implements ZoneRenderer<SphereZoneShape> {
	public static final SphereZoneRenderer INSTANCE = new SphereZoneRenderer();

	@Override
	public void render(SphereZoneShape shape, Context ctx) {
		var ms = ctx.poseStack();

		var box = shape.getBoundingBox();
		float minX = (float) (box.minX - ctx.cameraPos().x);
		float minY = (float) (box.minY - ctx.cameraPos().y);
		float minZ = (float) (box.minZ - ctx.cameraPos().z);
		float maxX = (float) (box.maxX - ctx.cameraPos().x);
		float maxY = (float) (box.maxY - ctx.cameraPos().y);
		float maxZ = (float) (box.maxZ - ctx.cameraPos().z);
		BoxRenderer.renderDebugLines(minX, minY, minZ, maxX, maxY, maxZ, ms, ctx.buffers(), Color.WHITE);

		ms.pushPose();
		ms.translate(shape.pos().x - ctx.cameraPos().x, shape.pos().y - ctx.cameraPos().y, shape.pos().z - ctx.cameraPos().z);
		float scale = (float) (shape.radius() * 2D);
		ms.scale(scale, scale, scale);
		SphereRenderer.renderDebugLines(SpherePoints.M, ms, ctx.buffers(), ctx.outlineColor());
		SphereRenderer.renderDebugQuads(SpherePoints.M, ms, ctx.buffers(), false, ctx.color());
		ms.popPose();
	}
}
