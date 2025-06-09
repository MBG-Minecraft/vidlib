package dev.latvian.mods.vidlib.feature.zone.renderer;

import com.mojang.math.Axis;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.render.CuboidRenderer;
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
		CuboidRenderer.lines(ms, minX, minY, minZ, maxX, maxY, maxZ, ctx.buffers(), BufferSupplier.DEBUG_NO_DEPTH, Color.WHITE);

		ms.pushPose();
		ctx.frame().translate(shape.pos());
		ms.mulPose(Axis.YN.rotation(shape.rotation().yawRad()));
		ms.mulPose(Axis.XN.rotation(shape.rotation().pitchRad()));
		ms.mulPose(Axis.ZN.rotation(shape.rotation().roll()));
		ms.scale(shape.size().x(), shape.size().y(), shape.size().z());
		CuboidRenderer.quadsAndLines(ms, -0.5F, -0.5F, -0.5F, 0.5F, 0.5F, 0.5F, ctx.buffers(), BufferSupplier.DEBUG_NO_DEPTH, false, ctx.color(), ctx.outlineColor());
		ms.popPose();
	}
}
