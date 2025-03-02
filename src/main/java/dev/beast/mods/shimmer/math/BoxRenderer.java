package dev.beast.mods.shimmer.math;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class BoxRenderer {
	// POSITION_COLOR
	public static void renderDebugQuads(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, PoseStack ms, VertexConsumer buffer, Color color) {
		var e = ms.last();
		var m = e.pose();

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
	public static void renderDebugLines(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, PoseStack ms, VertexConsumer buffer, Color color) {
		var e = ms.last();
		var m = e.pose();

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
}