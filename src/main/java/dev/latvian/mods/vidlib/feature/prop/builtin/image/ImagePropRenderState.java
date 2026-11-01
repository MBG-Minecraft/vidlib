package dev.latvian.mods.vidlib.feature.prop.builtin.image;

import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.klib.util.client.EntityRenderTypes;
import dev.latvian.mods.vidlib.client.ExtractContext;
import dev.latvian.mods.vidlib.client.SubmitContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Matrix4f;

public class ImagePropRenderState extends PropRenderState<ImageProp> {
	public final Matrix4f bbMat = new Matrix4f();
	public float width;
	public float y;
	public Identifier texture;
	public UV uv;
	public boolean translucent;
	public int tint;
	public int light;

	@Override
	public void extract(ImageProp prop, ExtractContext ctx) {
		bbMat.identity();

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

		texture = prop.texture.getTexture();
		uv = ctx.mc().getSpriteUV(prop.texture);
		tint = prop.tint.argb();
		light = prop.fullBright ? LightCoordsUtil.FULL_BRIGHT : prop.getPackedLight();

		prop.rotatedQuadData.update(bbMat, width, y);
	}

	@Override
	public void submit(SubmitContext ctx) {
		var ms = ctx.poseStack();
		ms.pushPose();
		ms.mulPose(bbMat);

		ctx.collector().submitCustomGeometry(ms, EntityRenderTypes.texture(texture, translucent), (pose, buffer) -> {
			float w2 = width / 2F;
			buffer.addVertex(pose, -w2, y, 0F).setUv(uv.u1(), uv.v1()).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
			buffer.addVertex(pose, -w2, 0F, 0F).setUv(uv.u1(), uv.v0()).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
			buffer.addVertex(pose, w2, 0F, 0F).setUv(uv.u0(), uv.v0()).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
			buffer.addVertex(pose, w2, y, 0F).setUv(uv.u0(), uv.v1()).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
		});

		ms.popPose();
	}
}
