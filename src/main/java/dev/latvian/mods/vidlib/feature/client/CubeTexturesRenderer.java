package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.kmath.Directions;
import dev.latvian.mods.kmath.texture.LightUV;
import dev.latvian.mods.vidlib.util.CachedCube;
import dev.latvian.mods.vidlib.util.FaceTexture;
import dev.latvian.mods.vidlib.util.FrameInfo;
import dev.latvian.mods.vidlib.util.ResolvedCubeTextures;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;

public class CubeTexturesRenderer {
	public static void render(FrameInfo frame, LightUV light, CachedCube cube, TerrainRenderLayer renderLayerFilter) {
		var b = cube.box();
		render(frame, light, b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ, cube.textures(), renderLayerFilter);
	}

	public static void render(FrameInfo frame, LightUV light, double bminX, double bminY, double bminZ, double bmaxX, double bmaxY, double bmaxZ, ResolvedCubeTextures textures, TerrainRenderLayer renderLayerFilter) {
		var ms = frame.poseStack();
		var msp = ms.last();
		var m = msp.pose();
		var buffers = frame.buffers();
		var n = frame.normal();

		var lu = light.lightU();
		var lv = light.lightV();

		float minX = frame.x(bminX);
		float minY = frame.y(bminY);
		float minZ = frame.z(bminZ);
		float maxX = frame.x(bmaxX);
		float maxY = frame.y(bmaxY);
		float maxZ = frame.z(bmaxZ);

		for (int direction = 0; direction < 6; direction++) {
			var face = textures.faces().get(direction);

			if (face == null || face == FaceTexture.EMPTY || face.layer() != renderLayerFilter) {
				continue;
			}

			var colR = face.tint().redf();
			var colG = face.tint().greenf();
			var colB = face.tint().bluef();
			var colA = face.tint().alphaf();

			var scale = face.scale();
			float tw = scale <= 0F ? 1F : (maxX - minX) * scale;
			float th = scale <= 0F ? 1F : (maxY - minY) * scale;
			float td = scale <= 0F ? 1F : (maxZ - minZ) * scale;

			var texture = DynamicSpriteTexture.get(frame.mc(), face.sprite());
			var buffer = buffers.getBuffer(VidLibRenderTypes.Terrain.get(face.layer(), face.cull()).apply(texture.resourceId()));

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
