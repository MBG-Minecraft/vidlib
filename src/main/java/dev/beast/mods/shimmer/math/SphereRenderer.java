package dev.beast.mods.shimmer.math;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector3f;

public class SphereRenderer {
	// POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
	public static void renderEntity(SpherePoints points, PoseStack ms, VertexConsumer buffer, Color color, UV uv, LightUV light) {
		var e = ms.last();
		var m = e.pose();

		var u0 = uv.u0();
		var v0 = uv.v0();
		var u1 = uv.u1();
		var v1 = uv.v1();

		int ou = light.overlayU;
		int ov = light.overlayV;
		int lu = light.lightU;
		int lv = light.lightV;

		float colR = color.redf();
		float colG = color.greenf();
		float colB = color.bluef();
		float colA = color.alphaf();

		var tempNormal = new Vector3f();

		for (int r = 0; r < points.rows.length - 1; r++) {
			for (int c = 0; c < points.cols.length - 1; c++) {
				var cr = points.rows[r];
				var nr = points.rows[r + 1];
				var cc = points.cols[c];
				var nc = points.cols[c + 1];
				var nv = e.transformNormal(points.normals[c][r], tempNormal);

				var u0l = KMath.lerp(cc.u(), u0, u1);
				var v0l = KMath.lerp(cr.v(), v0, v1);
				var u1l = KMath.lerp(nc.u(), u0, u1);
				var v1l = KMath.lerp(nr.v(), v0, v1);

				buffer.addVertex(m, cc.x() * nr.m(), nr.y(), cc.z() * nr.m()).setColor(colR, colG, colB, colA).setUv(u0l, v1l).setUv1(ou, ov).setUv2(lu, lv).setNormal(nv.x, nv.y, nv.z);
				buffer.addVertex(m, cc.x() * cr.m(), cr.y(), cc.z() * cr.m()).setColor(colR, colG, colB, colA).setUv(u0l, v0l).setUv1(ou, ov).setUv2(lu, lv).setNormal(nv.x, nv.y, nv.z);
				buffer.addVertex(m, nc.x() * cr.m(), cr.y(), nc.z() * cr.m()).setColor(colR, colG, colB, colA).setUv(u1l, v0l).setUv1(ou, ov).setUv2(lu, lv).setNormal(nv.x, nv.y, nv.z);
				buffer.addVertex(m, nc.x() * nr.m(), nr.y(), nc.z() * nr.m()).setColor(colR, colG, colB, colA).setUv(u1l, v1l).setUv1(ou, ov).setUv2(lu, lv).setNormal(nv.x, nv.y, nv.z);
			}
		}
	}

	public static void renderEntity(SpherePoints points, PoseStack ms, VertexConsumer buffer) {
		renderEntity(points, ms, buffer, Color.WHITE, UV.FULL, LightUV.NORMAL);
	}

	// POSITION_COLOR
	public static void renderDebugQuads(SpherePoints points, PoseStack ms, VertexConsumer buffer, Color color) {
		var e = ms.last();
		var m = e.pose();

		float colR = color.redf();
		float colG = color.greenf();
		float colB = color.bluef();
		float colA = color.alphaf();

		for (int r = 0; r < points.rows.length - 1; r++) {
			for (int c = 0; c < points.cols.length - 1; c++) {
				var cr = points.rows[r];
				var nr = points.rows[r + 1];
				var cc = points.cols[c];
				var nc = points.cols[c + 1];

				buffer.addVertex(m, cc.x() * nr.m(), nr.y(), cc.z() * nr.m()).setColor(colR, colG, colB, colA);
				buffer.addVertex(m, cc.x() * cr.m(), cr.y(), cc.z() * cr.m()).setColor(colR, colG, colB, colA);
				buffer.addVertex(m, nc.x() * cr.m(), cr.y(), nc.z() * cr.m()).setColor(colR, colG, colB, colA);
				buffer.addVertex(m, nc.x() * nr.m(), nr.y(), nc.z() * nr.m()).setColor(colR, colG, colB, colA);
			}
		}
	}

	// POSITION_COLOR
	public static void renderDebugLines(SpherePoints points, PoseStack ms, VertexConsumer buffer, Color color) {
		var e = ms.last();
		var m = e.pose();

		float colR = color.redf();
		float colG = color.greenf();
		float colB = color.bluef();
		float colA = color.alphaf();

		for (int r = 0; r < points.rows.length - 1; r++) {
			for (int c = 0; c < points.cols.length - 1; c++) {
				var cr = points.rows[r];
				var nr = points.rows[r + 1];
				var cc = points.cols[c];
				var nc = points.cols[c + 1];

				buffer.addVertex(m, cc.x() * nr.m(), nr.y(), cc.z() * nr.m()).setColor(colR, colG, colB, colA);
				buffer.addVertex(m, cc.x() * cr.m(), cr.y(), cc.z() * cr.m()).setColor(colR, colG, colB, colA);

				buffer.addVertex(m, cc.x() * cr.m(), cr.y(), cc.z() * cr.m()).setColor(colR, colG, colB, colA);
				buffer.addVertex(m, nc.x() * cr.m(), cr.y(), nc.z() * cr.m()).setColor(colR, colG, colB, colA);
			}
		}
	}
}