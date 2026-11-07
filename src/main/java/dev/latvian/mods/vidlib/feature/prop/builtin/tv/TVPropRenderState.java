package dev.latvian.mods.vidlib.feature.prop.builtin.tv;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.klib.color.TransparencyLevel;
import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.client.EntityRenderTypes;
import dev.latvian.mods.vidlib.feature.client.VidLibExtractContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Matrix4f;

public class TVPropRenderState extends PropRenderState<TVProp> {
	public static final RenderType TEXTURE_BG = EntityRenderTypes.texture(new ClientAsset.ResourceTexture(ID.vidlib("prop/tv/bg")), TransparencyLevel.SOLID);
	public static final RenderType TEXTURE_FG = EntityRenderTypes.texture(new ClientAsset.ResourceTexture(ID.vidlib("prop/tv/fg")), TransparencyLevel.SOLID);
	public static final RenderType TEXTURE_VIDEO = EntityRenderTypes.texture(TVPlayer.TEXTURE, TransparencyLevel.TRANSLUCENT);
	public static final RenderType TEXTURE_NO_VIDEO = EntityRenderTypes.texture(new ClientAsset.ResourceTexture(ID.vidlib("prop/tv/no_video")), TransparencyLevel.TRANSLUCENT);

	public final Matrix4f bbMat = new Matrix4f();
	public float width;
	public float height;
	public int light;
	public float offset;
	public float px;
	public int tint;
	public float videoW;
	public float videoH;
	public float videoS;

	@Override
	public void extract(TVProp prop, VidLibExtractContext ctx) {
		float delta = ctx.delta();

		width = (float) prop.width;
		height = (float) prop.height;

		bbMat.identity();
		bbMat.rotateY((float) Math.toRadians(-prop.getYaw(delta)));
		bbMat.rotateX((float) Math.toRadians(-prop.getPitch(delta)));

		if (!prop.centered) {
			bbMat.translate(0F, height / 2F, 0F);
		}

		light = prop.fullBright ? LightCoordsUtil.FULL_BRIGHT : prop.getPackedLight();

		offset = Math.clamp((float) ((squaredDistanceToCamera - 1000D) / 1000D), 0.01F, 0.5F);
		px = 1F / 16F;
		tint = 0xFFFFFFFF;
		videoW = (float) prop.videoWidth;
		videoH = (float) prop.videoHeight;
		videoS = Math.min(width / videoW, height / videoH);

		prop.rotatedQuadData.update(bbMat, width, height);
	}

	@Override
	public void submit(SubmitContext ctx) {
		ctx.poseStack().mulPose(bbMat);

		ctx.submit(TEXTURE_BG, (pose, buffer) -> {
			cq(buffer, pose, 0F, -(height - px) / 2F, 0F, width, px);
			cq(buffer, pose, 0F, (height - px) / 2F, 0F, width, px);
			cq(buffer, pose, -(width - px) / 2F, 0F, 0F, px, height - px);
			cq(buffer, pose, (width - px) / 2F, 0F, 0F, px, height - px);
		});

		ctx.submit(TEXTURE_FG, (pose, buffer) -> cq(buffer, pose, 0F, 0F, 0F, width - px * 2F, height - px * 2F));

		if (TVPlayer.hasVideo()) {
			ctx.submit(TEXTURE_VIDEO, (pose, buffer) -> cq(buffer, pose, 0F, 0F, offset, videoW * videoS - px * 2F, videoH * videoS - px * 2F));
		} else {
			ctx.submit(TEXTURE_NO_VIDEO, (pose, buffer) -> {
				float s = Math.min(videoW * videoS - px * 2F, videoH * videoS - px * 2F);
				cq(buffer, pose, 0F, 0F, offset, s, s);
			});
		}
	}

	public void cq(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float w, float h) {
		if (ARGB.alpha(tint) <= 0) {
			return;
		}

		var m = pose.pose();
		consumer.addVertex(m, x - w / 2F, y + h / 2F, z).setColor(tint).setUv(0F, 0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0F, 1F, 0F);
		consumer.addVertex(m, x - w / 2F, y - h / 2F, z).setColor(tint).setUv(0F, 1F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0F, 1F, 0F);
		consumer.addVertex(m, x + w / 2F, y - h / 2F, z).setColor(tint).setUv(1F, 1F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0F, 1F, 0F);
		consumer.addVertex(m, x + w / 2F, y + h / 2F, z).setColor(tint).setUv(1F, 0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0F, 1F, 0F);
	}
}
