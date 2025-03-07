package dev.beast.mods.shimmer.math;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.beast.mods.shimmer.util.ShimmerRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

public class BoxRenderer {
	// POSITION_COLOR
	public static void renderDebugQuads(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, PoseStack ms, MultiBufferSource buffers, boolean cull, Color color) {
		var e = ms.last();
		var m = e.pose();
		var buffer = buffers.getBuffer(cull ? ShimmerRenderTypes.DEBUG_QUADS : ShimmerRenderTypes.DEBUG_QUADS_NO_CULL);

		float colR = color.redf();
		float colG = color.greenf();
		float colB = color.bluef();
		float colA = color.alphaf();

		// East
		buffer.addVertex(m, maxX, maxY, minZ).setColor(colR, colG, colB, colA); // N 1 0 0
		buffer.addVertex(m, maxX, maxY, maxZ).setColor(colR, colG, colB, colA); // N 1 0 0
		buffer.addVertex(m, maxX, minY, maxZ).setColor(colR, colG, colB, colA); // N 1 0 0
		buffer.addVertex(m, maxX, minY, minZ).setColor(colR, colG, colB, colA); // N 1 0 0

		// South
		buffer.addVertex(m, maxX, minY, maxZ).setColor(colR, colG, colB, colA); // N 0 0 1
		buffer.addVertex(m, maxX, maxY, maxZ).setColor(colR, colG, colB, colA); // N 0 0 1
		buffer.addVertex(m, minX, maxY, maxZ).setColor(colR, colG, colB, colA); // N 0 0 1
		buffer.addVertex(m, minX, minY, maxZ).setColor(colR, colG, colB, colA); // N 0 0 1

		// North
		buffer.addVertex(m, minX, minY, minZ).setColor(colR, colG, colB, colA); // N 0 0 -1
		buffer.addVertex(m, minX, maxY, minZ).setColor(colR, colG, colB, colA); // N 0 0 -1
		buffer.addVertex(m, maxX, maxY, minZ).setColor(colR, colG, colB, colA); // N 0 0 -1
		buffer.addVertex(m, maxX, minY, minZ).setColor(colR, colG, colB, colA); // N 0 0 -1

		// West
		buffer.addVertex(m, minX, minY, minZ).setColor(colR, colG, colB, colA); // N -1 0 0
		buffer.addVertex(m, minX, minY, maxZ).setColor(colR, colG, colB, colA); // N -1 0 0
		buffer.addVertex(m, minX, maxY, maxZ).setColor(colR, colG, colB, colA); // N -1 0 0
		buffer.addVertex(m, minX, maxY, minZ).setColor(colR, colG, colB, colA); // N -1 0 0

		// Up
		buffer.addVertex(m, minX, maxY, minZ).setColor(colR, colG, colB, colA); // N 0 1 0
		buffer.addVertex(m, minX, maxY, maxZ).setColor(colR, colG, colB, colA); // N 0 1 0
		buffer.addVertex(m, maxX, maxY, maxZ).setColor(colR, colG, colB, colA); // N 0 1 0
		buffer.addVertex(m, maxX, maxY, minZ).setColor(colR, colG, colB, colA); // N 0 1 0

		// Down
		buffer.addVertex(m, minX, minY, minZ).setColor(colR, colG, colB, colA); // N 0 -1 0
		buffer.addVertex(m, maxX, minY, minZ).setColor(colR, colG, colB, colA); // N 0 -1 0
		buffer.addVertex(m, maxX, minY, maxZ).setColor(colR, colG, colB, colA); // N 0 -1 0
		buffer.addVertex(m, minX, minY, maxZ).setColor(colR, colG, colB, colA); // N 0 -1 0

	}

	// POSITION_COLOR
	public static void renderDebugLines(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, PoseStack ms, MultiBufferSource buffers, Color color) {
		var e = ms.last();
		var m = e.pose();
		var buffer = buffers.getBuffer(ShimmerRenderTypes.DEBUG_LINES);

		float colR = color.redf();
		float colG = color.greenf();
		float colB = color.bluef();
		float colA = color.alphaf();

		buffer.addVertex(m, minX, minY, minZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, maxX, minY, minZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, minX, minY, minZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, minX, maxY, minZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, minX, minY, minZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, minX, minY, maxZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, maxX, minY, minZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, maxX, maxY, minZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, maxX, maxY, minZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, minX, maxY, minZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, minX, maxY, minZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, minX, maxY, maxZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, minX, maxY, maxZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, minX, minY, maxZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, minX, minY, maxZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, maxX, minY, maxZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, maxX, minY, maxZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, maxX, minY, minZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, minX, maxY, maxZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, maxX, maxY, maxZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, maxX, minY, maxZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, maxX, maxY, maxZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, maxX, maxY, minZ).setColor(colR, colG, colB, colA);
		buffer.addVertex(m, maxX, maxY, maxZ).setColor(colR, colG, colB, colA);
	}

	// POSITION_COLOR
	public static void renderVoxelShape(PoseStack ms, MultiBufferSource buffers, VoxelShapeBox shape, Vec3 offset, boolean cull, Color color, Color lineColor) {
		if (shape == VoxelShapeBox.EMPTY) {
			return;
		}

		var e = ms.last();
		var m = e.pose();
		var buffer = buffers.getBuffer(ShimmerRenderTypes.DEBUG_LINES);

		float lcolR = lineColor.redf();
		float lcolG = lineColor.greenf();
		float lcolB = lineColor.bluef();
		float lcolA = lineColor.alphaf();

		for (var edge : shape.edges()) {
			float minX = (float) (edge.start().x + offset.x);
			float minY = (float) (edge.start().y + offset.y);
			float minZ = (float) (edge.start().z + offset.z);
			float maxX = (float) (edge.end().x + offset.x);
			float maxY = (float) (edge.end().y + offset.y);
			float maxZ = (float) (edge.end().z + offset.z);

			buffer.addVertex(m, minX, minY, minZ).setColor(lcolR, lcolG, lcolB, lcolA);
			buffer.addVertex(m, maxX, maxY, maxZ).setColor(lcolR, lcolG, lcolB, lcolA);
		}

		for (var box : shape.boxes()) {
			float minX = (float) (box.minX + offset.x);
			float minY = (float) (box.minY + offset.y);
			float minZ = (float) (box.minZ + offset.z);
			float maxX = (float) (box.maxX + offset.x);
			float maxY = (float) (box.maxY + offset.y);
			float maxZ = (float) (box.maxZ + offset.z);
			renderDebugQuads(minX, minY, minZ, maxX, maxY, maxZ, ms, buffers, cull, color);
		}
	}
}