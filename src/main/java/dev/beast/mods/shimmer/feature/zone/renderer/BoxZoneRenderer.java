package dev.beast.mods.shimmer.feature.zone.renderer;

import dev.beast.mods.shimmer.feature.zone.ZoneShape;
import dev.beast.mods.shimmer.math.BoxRenderer;
import dev.beast.mods.shimmer.util.ShimmerRenderTypes;

public class BoxZoneRenderer implements ZoneRenderer<ZoneShape> {
	public static final BoxZoneRenderer INSTANCE = new BoxZoneRenderer();

	@Override
	public void render(ZoneShape shape, Context ctx) {
		var box = shape.getBoundingBox();
		float minX = (float) (box.minX - ctx.cameraPos().x);
		float minY = (float) (box.minY - ctx.cameraPos().y);
		float minZ = (float) (box.minZ - ctx.cameraPos().z);
		float maxX = (float) (box.maxX - ctx.cameraPos().x);
		float maxY = (float) (box.maxY - ctx.cameraPos().y);
		float maxZ = (float) (box.maxZ - ctx.cameraPos().z);

		BoxRenderer.renderDebugLines(minX, minY, minZ, maxX, maxY, maxZ, ctx.poseStack(), ctx.buffers().getBuffer(ShimmerRenderTypes.DEBUG_LINES), ctx.outlineColor());
		BoxRenderer.renderDebugQuads(minX, minY, minZ, maxX, maxY, maxZ, ctx.poseStack(), ctx.buffers().getBuffer(ShimmerRenderTypes.DEBUG_QUADS_NO_CULL), ctx.color());
	}
}
