package dev.beast.mods.shimmer.feature.zone.renderer;

import com.mojang.math.Axis;
import dev.beast.mods.shimmer.feature.zone.shape.RotatedBoxZoneShape;
import dev.beast.mods.shimmer.math.BoxRenderer;
import dev.beast.mods.shimmer.math.Color;

public class RotatedBoxZoneRenderer implements ZoneRenderer<RotatedBoxZoneShape> {
	@Override
	public void render(RotatedBoxZoneShape shape, Context ctx) {
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
		ms.mulPose(Axis.YN.rotation(shape.rotation().yawRad()));
		ms.mulPose(Axis.XN.rotation(shape.rotation().pitchRad()));
		ms.mulPose(Axis.ZN.rotation(shape.rotation().roll()));
		ms.scale(shape.size().x(), shape.size().y(), shape.size().z());
		BoxRenderer.renderDebugLines(-0.5F, -0.5F, -0.5F, 0.5F, 0.5F, 0.5F, ms, ctx.buffers(), ctx.outlineColor());
		BoxRenderer.renderDebugQuads(-0.5F, -0.5F, -0.5F, 0.5F, 0.5F, 0.5F, ms, ctx.buffers(), false, ctx.color());
		ms.popPose();
	}
}
