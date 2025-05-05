package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.texture.LightUV;
import dev.latvian.mods.vidlib.util.FrameInfo;
import net.minecraft.world.level.material.FluidState;
import org.joml.Vector3f;

public class FluidBoxRenderer {
	public static void render(FrameInfo frame, FluidState fluidState, Color color, LightUV light, double bminX, double bminY, double bminZ, double bmaxX, double bmaxY, double bmaxZ, DynamicSpriteTexture stillTexture, DynamicSpriteTexture flowingTexture) {
		var ms = frame.poseStack();
		var msp = ms.last();
		var m = msp.pose();
		var buffers = frame.buffers();

		var n = new Vector3f();

		var colR = color.redf();
		var colG = color.greenf();
		var colB = color.bluef();
		var colA = color.alphaf();

		var lu = light.lightU();
		var lv = light.lightV();

		float minX = frame.x(bminX);
		float minY = frame.y(bminY);
		float minZ = frame.z(bminZ);
		float maxX = frame.x(bmaxX);
		float maxY = frame.y(bmaxY);
		float maxZ = frame.z(bmaxZ);

		float tw = maxX - minX;
		float th = maxY - minY;
		float td = maxZ - minZ;

		var buffer = buffers.getBuffer(VidLibRenderTypes.getFluid(fluidState, stillTexture, false));

		// Up
		msp.transformNormal(0F, 1F, 0F, n);
		buffer.addVertex(m, minX, maxY, minZ).setColor(colR, colG, colB, colA).setUv(0F, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, minX, maxY, maxZ).setColor(colR, colG, colB, colA).setUv(0F, td).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, maxX, maxY, maxZ).setColor(colR, colG, colB, colA).setUv(tw, td).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, maxX, maxY, minZ).setColor(colR, colG, colB, colA).setUv(tw, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);

		buffer = buffers.getBuffer(VidLibRenderTypes.getFluid(fluidState, stillTexture, true));

		// Down
		msp.transformNormal(0F, -1F, 0F, n);
		buffer.addVertex(m, minX, minY, minZ).setColor(colR, colG, colB, colA).setUv(0F, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, maxX, minY, minZ).setColor(colR, colG, colB, colA).setUv(tw, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, maxX, minY, maxZ).setColor(colR, colG, colB, colA).setUv(tw, td).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, minX, minY, maxZ).setColor(colR, colG, colB, colA).setUv(0F, td).setUv2(lu, lv).setNormal(n.x, n.y, n.z);

		buffer = buffers.getBuffer(VidLibRenderTypes.getFluid(fluidState, flowingTexture, true));

		float sw = tw / 2F;
		float sh = th / 2F;
		float sd = td / 2F;

		// East
		msp.transformNormal(1F, 0F, 0F, n);
		buffer.addVertex(m, maxX, maxY, minZ).setColor(colR, colG, colB, colA).setUv(0F, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, maxX, maxY, maxZ).setColor(colR, colG, colB, colA).setUv(sd, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, maxX, minY, maxZ).setColor(colR, colG, colB, colA).setUv(sd, sh).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, maxX, minY, minZ).setColor(colR, colG, colB, colA).setUv(0F, sh).setUv2(lu, lv).setNormal(n.x, n.y, n.z);

		// South
		msp.transformNormal(0F, 0F, 1F, n);
		buffer.addVertex(m, maxX, minY, maxZ).setColor(colR, colG, colB, colA).setUv(sw, sh).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, maxX, maxY, maxZ).setColor(colR, colG, colB, colA).setUv(sw, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, minX, maxY, maxZ).setColor(colR, colG, colB, colA).setUv(0F, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, minX, minY, maxZ).setColor(colR, colG, colB, colA).setUv(0F, sh).setUv2(lu, lv).setNormal(n.x, n.y, n.z);

		// North
		msp.transformNormal(0F, 0F, -1F, n);
		buffer.addVertex(m, minX, minY, minZ).setColor(colR, colG, colB, colA).setUv(0F, sh).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, minX, maxY, minZ).setColor(colR, colG, colB, colA).setUv(0F, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, maxX, maxY, minZ).setColor(colR, colG, colB, colA).setUv(sw, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, maxX, minY, minZ).setColor(colR, colG, colB, colA).setUv(sw, sh).setUv2(lu, lv).setNormal(n.x, n.y, n.z);

		// West
		msp.transformNormal(-1F, 0F, 0F, n);
		buffer.addVertex(m, minX, minY, minZ).setColor(colR, colG, colB, colA).setUv(0F, sh).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, minX, minY, maxZ).setColor(colR, colG, colB, colA).setUv(sd, sh).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, minX, maxY, maxZ).setColor(colR, colG, colB, colA).setUv(sd, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
		buffer.addVertex(m, minX, maxY, minZ).setColor(colR, colG, colB, colA).setUv(0F, 0F).setUv2(lu, lv).setNormal(n.x, n.y, n.z);
	}
}
