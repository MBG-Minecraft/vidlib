package dev.latvian.mods.vidlib.feature.texture;

import dev.latvian.mods.kmath.Directions;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.texture.LightUV;
import dev.latvian.mods.vidlib.feature.client.TerrainRenderTypes;
import dev.latvian.mods.vidlib.util.client.FrameInfo;

public class TexturedCubeRenderer {
	public static void render(FrameInfo frame, LightUV light, ResolvedTexturedCube cube, Color tint) {
		var b = cube.box();
		render(frame, light, b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ, cube.textures(), tint);
	}

	public static void render(FrameInfo frame, LightUV light, double bminX, double bminY, double bminZ, double bmaxX, double bmaxY, double bmaxZ, ResolvedCubeTextures textures, Color tint) {
		var ms = frame.poseStack();
		var msp = ms.last();
		var m = msp.pose();
		var buffers = frame.buffers();
		var n = frame.normal();

		var lu = light.u();
		var lv = light.v();

		float minX = frame.x(bminX);
		float minY = frame.y(bminY);
		float minZ = frame.z(bminZ);
		float maxX = frame.x(bmaxX);
		float maxY = frame.y(bmaxY);
		float maxZ = frame.z(bmaxZ);

		for (int direction = 0; direction < 6; direction++) {
			var face = textures.faces().get(direction);

			if (face == null || face == FaceTexture.EMPTY || !frame.is(face.layer())) {
				continue;
			}

			var colA = face.tint().alphaf() * tint.alphaf();

			if (face.fade() > 0D) {
				double d = Math.sqrt(frame.distanceSq(bminX, bminY, bminZ, bmaxX, bmaxY, bmaxZ));

				if (d < face.fade()) {
					colA = (float) (colA * d / face.fade());
				}
			}

			if (colA <= 0.01F) {
				continue;
			}

			var colR = face.tint().redf() * tint.redf();
			var colG = face.tint().greenf() * tint.greenf();
			var colB = face.tint().bluef() * tint.bluef();

			var uvScale = face.uvScale();
			float tw = uvScale <= 0F ? 1F : (maxX - minX) * uvScale;
			float th = uvScale <= 0F ? 1F : (maxY - minY) * uvScale;
			float td = uvScale <= 0F ? 1F : (maxZ - minZ) * uvScale;

			var texture = DynamicSpriteTexture.get(face.sprite());
			var buffer = buffers.getBuffer(TerrainRenderTypes.get(face.layer(), face.cull()).apply(texture));

			msp.transformNormal(Directions.ALL[direction].getUnitVec3f(), n);

			switch (direction) {
				case 0 -> {
					buffer.addVertex(m, minX, minY, minZ).setColor(colR, colG, colB, colA).setUv(0F, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, maxX, minY, minZ).setColor(colR, colG, colB, colA).setUv(tw, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, maxX, minY, maxZ).setColor(colR, colG, colB, colA).setUv(tw, td).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, minX, minY, maxZ).setColor(colR, colG, colB, colA).setUv(0F, td).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
				}
				case 1 -> {
					buffer.addVertex(m, minX, maxY, minZ).setColor(colR, colG, colB, colA).setUv(0F, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, minX, maxY, maxZ).setColor(colR, colG, colB, colA).setUv(0F, td).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, maxX, maxY, maxZ).setColor(colR, colG, colB, colA).setUv(tw, td).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, maxX, maxY, minZ).setColor(colR, colG, colB, colA).setUv(tw, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
				}
				case 2 -> {
					buffer.addVertex(m, minX, minY, minZ).setColor(colR, colG, colB, colA).setUv(tw, th).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, minX, maxY, minZ).setColor(colR, colG, colB, colA).setUv(tw, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, maxX, maxY, minZ).setColor(colR, colG, colB, colA).setUv(0F, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, maxX, minY, minZ).setColor(colR, colG, colB, colA).setUv(0F, th).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
				}
				case 3 -> {
					buffer.addVertex(m, maxX, minY, maxZ).setColor(colR, colG, colB, colA).setUv(tw, th).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, maxX, maxY, maxZ).setColor(colR, colG, colB, colA).setUv(tw, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, minX, maxY, maxZ).setColor(colR, colG, colB, colA).setUv(0F, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, minX, minY, maxZ).setColor(colR, colG, colB, colA).setUv(0F, th).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
				}
				case 4 -> {
					buffer.addVertex(m, minX, minY, minZ).setColor(colR, colG, colB, colA).setUv(0F, th).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, minX, minY, maxZ).setColor(colR, colG, colB, colA).setUv(td, th).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, minX, maxY, maxZ).setColor(colR, colG, colB, colA).setUv(td, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, minX, maxY, minZ).setColor(colR, colG, colB, colA).setUv(0F, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
				}
				case 5 -> {
					buffer.addVertex(m, maxX, maxY, minZ).setColor(colR, colG, colB, colA).setUv(td, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, maxX, maxY, maxZ).setColor(colR, colG, colB, colA).setUv(0F, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, maxX, minY, maxZ).setColor(colR, colG, colB, colA).setUv(0F, th).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
					buffer.addVertex(m, maxX, minY, minZ).setColor(colR, colG, colB, colA).setUv(td, th).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
				}
			}
		}
	}
}
