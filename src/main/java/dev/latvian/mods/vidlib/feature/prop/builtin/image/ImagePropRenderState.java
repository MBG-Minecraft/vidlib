package dev.latvian.mods.vidlib.feature.prop.builtin.image;

import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.klib.texture.SpriteRenderState;
import dev.latvian.mods.vidlib.feature.client.VidLibExtractContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Matrix4f;

public class ImagePropRenderState extends PropRenderState<ImageProp> {
	public final Matrix4f bbMat = new Matrix4f();
	public float width;
	public float y;
	public final SpriteRenderState sprite = new SpriteRenderState();
	public int tint;
	public int light;

	@Override
	public void extract(ImageProp prop, VidLibExtractContext ctx) {
		float delta = ctx.delta();

		width = (float) prop.width;
		float height = (float) prop.height;
		y = -height;

		bbMat.identity();
		// bbMat.scale(width, height, 1F);
		bbMat.rotateY((float) Math.toRadians(180F - prop.getYaw(delta)));
		bbMat.rotateX((float) Math.toRadians(prop.getPitch(delta)));

		if (prop.centered) {
			bbMat.translate(0F, height / 2F, 0F);
		}

		sprite.extract(ctx.mc(), prop.texture);
		tint = prop.tint.argb();
		light = prop.fullBright ? LightCoordsUtil.FULL_BRIGHT : prop.getPackedLight();

		prop.rotatedQuadData.update(bbMat, width, y);
	}

	@Override
	public void submit(SubmitContext ctx) {
		ctx.poseStack().mulPose(bbMat);

		ctx.submit(sprite.getEntityRenderType(false, ARGB.alpha(tint)), (pose, buffer) -> {
			float w2 = width / 2F;
			buffer.addVertex(pose, -w2, y, 0F).setUv(sprite.uv.u1(), sprite.uv.v1()).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
			buffer.addVertex(pose, -w2, 0F, 0F).setUv(sprite.uv.u1(), sprite.uv.v0()).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
			buffer.addVertex(pose, w2, 0F, 0F).setUv(sprite.uv.u0(), sprite.uv.v0()).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
			buffer.addVertex(pose, w2, y, 0F).setUv(sprite.uv.u0(), sprite.uv.v1()).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
		});
	}
}
