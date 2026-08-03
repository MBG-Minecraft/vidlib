package dev.latvian.mods.vidlib.feature.prop.builtin.tv;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.EntityRenderTypes;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TVPropRenderer implements PropRenderer<TVProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = PropRenderer.holder(TVProp.TYPE, new TVPropRenderer());

	public static final ResourceLocation TEXTURE_BG = VidLib.id("textures/prop/tv/bg.png");
	public static final ResourceLocation TEXTURE_FG = VidLib.id("textures/prop/tv/fg.png");
	public static final ResourceLocation TEXTURE_NO_VIDEO = VidLib.id("textures/prop/tv/no_video.png");

	@Override
	public void render(PropRenderContext<TVProp> ctx) {
		var prop = ctx.prop();
		float delta = ctx.delta();

		float w = (float) prop.width;
		float h = (float) prop.height;
		float px = 1F / 16F;

		var bbMat = new Matrix4f();
		// bbMat.translate(0F, -h / 2F, 0F);

		// bbMat.scale(width, height, 1F);
		bbMat.rotateY((float) Math.toRadians(-prop.getYaw(delta)));
		bbMat.rotateX((float) Math.toRadians(-prop.getPitch(delta)));

		if (!prop.centered) {
			bbMat.translate(0F, h / 2F, 0F);
		}

		var m = ctx.frame().poseStack().last().pose();
		m.mul(bbMat);

		var n = new Vector3f(0F, 1F, 0F);

		int tint = 0xFFFFFFFF;
		int light = prop.getPackedLight();

		var bg = ctx.frame().buffers().getBuffer(EntityRenderTypes.texture(TEXTURE_BG, false));
		cq(bg, m, 0F, -(h - px) / 2F, 0F, w, px, n, tint, light);
		cq(bg, m, 0F, (h - px) / 2F, 0F, w, px, n, tint, light);

		cq(bg, m, -(w - px) / 2F, 0F, 0F, px, h - px, n, tint, light);
		cq(bg, m, (w - px) / 2F, 0F, 0F, px, h - px, n, tint, light);

		float videoW = (float) prop.videoWidth;
		float videoH = (float) prop.videoHeight;
		float videoS = Math.min(w / videoW, h / videoH);

		var fg = ctx.frame().buffers().getBuffer(EntityRenderTypes.texture(TEXTURE_FG, false));
		cq(fg, m, 0F, 0F, 0F, w - px * 2F, h - px * 2F, n, tint, light);
		renderTVImage(ctx, m, videoW, videoH, videoS, px, n, tint, prop.fullBright ? LightTexture.FULL_BRIGHT : light);

		prop.rotatedQuadData.update(bbMat, w, h);
	}

	public static void cq(VertexConsumer consumer, Matrix4f m, float x, float y, float z, float w, float h, Vector3f n, int color, int light) {
		if (ARGB.alpha(color) <= 0) {
			return;
		}

		consumer.addVertex(m, x - w / 2F, y + h / 2F, z).setColor(color).setUv(0F, 0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(n.x, n.y, n.z);
		consumer.addVertex(m, x - w / 2F, y - h / 2F, z).setColor(color).setUv(0F, 1F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(n.x, n.y, n.z);
		consumer.addVertex(m, x + w / 2F, y - h / 2F, z).setColor(color).setUv(1F, 1F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(n.x, n.y, n.z);
		consumer.addVertex(m, x + w / 2F, y + h / 2F, z).setColor(color).setUv(1F, 0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(n.x, n.y, n.z);
	}

	private static void renderTVImage(PropRenderContext<TVProp> ctx, Matrix4f m, float videoW, float videoH, float videoS, float px, Vector3f n, int tint, int light) {
		if (TVPlayer.hasVideo()) {
			var video = ctx.frame().buffers().getBuffer(EntityRenderTypes.texture(TVPlayer.TEXTURE, false));
			cq(video, m, 0F, 0F, 0.01F, videoW * videoS - px * 2F, videoH * videoS - px * 2F, n, tint, light);
		} else {
			var video = ctx.frame().buffers().getBuffer(EntityRenderTypes.texture(TEXTURE_NO_VIDEO, false));
			float s = Math.min(videoW * videoS - px * 2F, videoH * videoS - px * 2F);
			cq(video, m, 0F, 0F, 0.01F, s, s, n, tint, light);
		}
	}
}
