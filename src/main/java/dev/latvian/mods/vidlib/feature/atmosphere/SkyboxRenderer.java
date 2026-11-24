package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import dev.latvian.mods.vidlib.core.api.client.VLSkyRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import org.joml.Matrix4fc;

public enum SkyboxRenderer implements CustomSkyboxRenderer {
	INSTANCE;

	@Override
	public boolean renderSky(LevelRenderState levelState, SkyRenderState skyState, Matrix4fc modelViewMatrix, GpuBufferSlice skyFog) {
		var vl = (VLSkyRenderState) skyState;
		var skyboxTextureData = vl.vl$skyboxTextureData();

		if (skyboxTextureData == null) {
			return false;
		}

		var mc = Minecraft.getInstance();
		SkyboxTexture skyboxTexture;

		if (mc.getTextureManager().byPath.get(skyboxTextureData.texture().generated().texturePath()) instanceof SkyboxTexture tex) {
			skyboxTexture = tex;
		} else {
			skyboxTexture = new SkyboxTexture(skyboxTextureData.texture());
			mc.getTextureManager().registerAndLoad(skyboxTexture.resourceId(), skyboxTexture);
		}

		skyboxTexture.getTexture(); // FIXME

		skyState.skyColor = ARGB.vector3fFromRGB24(0x3D91A9); // FIXME
		return false;
	}

	/*
	public boolean renderSky(Minecraft mc, LevelRenderState levelRenderState, Matrix4fc modelViewMatrix) {
		if (skybox == null) {
			return false;
		}

		float ps = levelRenderState.cameraRenderState.depthFar * 0.5F;
		float ns = -ps;

		var texture = loadTexture(mc);

		var ms = new PoseStack();
		// ms.mulPose(modelViewMatrix);
		ms.pushPose();

		if (skybox.rotating() != 0F) {
			float gameTime = mc.level == null ? 0F : (mc.level.getGameTime() % 24000L) / 24000F;
			ms.mulPose(Axis.YP.rotationDegrees(skybox.rotation() + 360F * gameTime * skybox.rotating()));
		}

		var buffer = mc.renderBuffers().bufferSource().getBuffer(VidLibRenderTypes.SKYBOX.apply(texture.resourceId()));
		var m = ms.last().pose();

		int cr = skybox.tint().red();
		int cg = skybox.tint().green();
		int cb = skybox.tint().blue();
		int ca = skybox.tint().alpha();

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
		return false;
	}
	 */
}
