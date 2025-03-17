package dev.beast.mods.shimmer.feature.skybox;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.beast.mods.shimmer.util.ShimmerRenderTypes;
import net.minecraft.client.Minecraft;

public class SkyboxRenderer {
	public static boolean render(Minecraft mc, Skybox skybox, Runnable setupFog) {
		setupFog.run();
		float ps = (float) (mc.gameRenderer.getDepthFar() / 2D / Math.sqrt(2D));
		float ns = -ps;

		var texture = skybox.loadTexture(mc);

		var ms = new PoseStack();
		// ms.mulPose(projectionMatrix);
		ms.pushPose();

		if (skybox.data.rotating() != 0F) {
			ms.mulPose(Axis.YP.rotationDegrees(360F * RenderSystem.getShaderGameTime() * skybox.data.rotating()));
		}

		var buffer = mc.renderBuffers().bufferSource().getBuffer(ShimmerRenderTypes.SKYBOX.apply(texture.resourceId()));
		var m = ms.last().pose();

		int cr = skybox.data.tint().red();
		int cg = skybox.data.tint().green();
		int cb = skybox.data.tint().blue();
		int ca = skybox.data.tint().alpha();

		// Up
		{
			float u0 = 0.25F;
			float v0 = 0F;
			float u1 = 0.5F;
			float v1 = 0.5F;
			buffer.addVertex(m, ns, ps, ns).setUv(u0, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ps, ns).setUv(u1, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ps, ps).setUv(u1, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ns, ps, ps).setUv(u0, v0).setColor(cr, cg, cb, ca);
		}

		// Down
		{
			float u0 = 0.5F;
			float v0 = 0F;
			float u1 = 0.75F;
			float v1 = 0.5F;
			buffer.addVertex(m, ns, ns, ns).setUv(u0, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ns, ns, ps).setUv(u0, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ns, ps).setUv(u1, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ns, ns).setUv(u1, v0).setColor(cr, cg, cb, ca);
		}

		float v0 = 0.5F;
		float v1 = 1F;

		// North
		{
			float u0 = 0.25F;
			float u1 = 0.5F;
			buffer.addVertex(m, ns, ns, ns).setUv(u0, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ns, ns).setUv(u1, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ps, ns).setUv(u1, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ns, ps, ns).setUv(u0, v0).setColor(cr, cg, cb, ca);
		}

		// South
		{
			float u0 = 0.75F;
			float u1 = 1F;
			buffer.addVertex(m, ns, ns, ps).setUv(u1, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ns, ps, ps).setUv(u1, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ps, ps).setUv(u0, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ns, ps).setUv(u0, v1).setColor(cr, cg, cb, ca);
		}

		// West
		{
			float u0 = 0F;
			float u1 = 0.25F;
			buffer.addVertex(m, ns, ns, ns).setUv(u1, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ns, ps, ns).setUv(u1, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ns, ps, ps).setUv(u0, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ns, ns, ps).setUv(u0, v1).setColor(cr, cg, cb, ca);
		}

		// East
		{
			float u0 = 0.5F;
			float u1 = 0.75F;
			buffer.addVertex(m, ps, ns, ns).setUv(u0, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ns, ps).setUv(u1, v1).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ps, ps).setUv(u1, v0).setColor(cr, cg, cb, ca);
			buffer.addVertex(m, ps, ps, ns).setUv(u0, v0).setColor(cr, cg, cb, ca);
		}

		ms.popPose();
		return !skybox.data.celestials();
	}
}
