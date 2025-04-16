package dev.latvian.mods.vidlib.feature.zone.renderer;

import com.mojang.math.Axis;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.render.BoxRenderer;
import dev.latvian.mods.vidlib.feature.zone.shape.RotatedBoxZoneShape;

public class RotatedBoxZoneRenderer implements ZoneRenderer<RotatedBoxZoneShape> {
	@Override
	public void render(RotatedBoxZoneShape shape, Context ctx) {
		var ms = ctx.frame().poseStack();

		var box = shape.getBoundingBox();
		float minX = ctx.frame().x(box.minX);
		float minY = ctx.frame().y(box.minY);
		float minZ = ctx.frame().z(box.minZ);
		float maxX = ctx.frame().x(box.maxX);
		float maxY = ctx.frame().y(box.maxY);
		float maxZ = ctx.frame().z(box.maxZ);
		BoxRenderer.renderDebugLines(minX, minY, minZ, maxX, maxY, maxZ, ms, ctx.buffers(), Color.WHITE);

		ms.pushPose();
		ctx.frame().translate(shape.pos());
		ms.mulPose(Axis.YN.rotation(shape.rotation().yawRad()));
		ms.mulPose(Axis.XN.rotation(shape.rotation().pitchRad()));
		ms.mulPose(Axis.ZN.rotation(shape.rotation().roll()));
		ms.scale(shape.size().x(), shape.size().y(), shape.size().z());
		BoxRenderer.renderDebugLines(-0.5F, -0.5F, -0.5F, 0.5F, 0.5F, 0.5F, ms, ctx.buffers(), ctx.outlineColor());
		BoxRenderer.renderDebugQuads(-0.5F, -0.5F, -0.5F, 0.5F, 0.5F, 0.5F, ms, ctx.buffers(), false, ctx.color());
		ms.popPose();
	}
}
