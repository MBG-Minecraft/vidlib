package dev.latvian.mods.vidlib.feature.prop.builtin.text;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.vidlib.feature.client.VidLibExtractContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Matrix4f;

public class TextPropRenderState extends PropRenderState<TextProp> {
	public final Matrix4f bbMat = new Matrix4f();
	public float width;
	public float y;
	public int light;
	public boolean shadow;
	public boolean seeThrough;
	public int wrap;
	public boolean fullBright;
	public Color backgroundColor;
	public boolean centered;
	public boolean autoRotateYaw;
	public boolean autoRotatePitch;
	public float lineHeight;
	public float shadowDistance;

	@Override
	public void extract(TextProp prop, VidLibExtractContext ctx) {
		float delta = ctx.delta();

		width = (float) prop.width;
		float height = (float) prop.height;
		y = -height;

		if (!prop.shadow) {
			y++;
		}

		float scale = 1F / -height * (float) prop.height;

		bbMat.identity();
		bbMat.scale(scale, scale, scale);
		bbMat.rotateY((float) Math.toRadians(180F - prop.getYaw(delta)));
		bbMat.rotateX((float) Math.toRadians(prop.getPitch(delta)));

		if (prop.centered) {
			bbMat.translate(0F, height / 2F, 0F);
		}

		light = prop.fullBright ? LightCoordsUtil.FULL_BRIGHT : prop.getPackedLight();
		shadow = prop.shadow;
		seeThrough = prop.seeThrough;
		wrap = prop.wrap;
		fullBright = prop.fullBright;
		backgroundColor = prop.backgroundColor;
		centered = prop.centered;
		autoRotateYaw = prop.autoRotateYaw;
		autoRotatePitch = prop.autoRotatePitch;
		lineHeight = prop.lineHeight;
		shadowDistance = prop.shadowDistance;
	}

	@Override
	public void submit(SubmitContext ctx) {
		ctx.poseStack().mulPose(bbMat);

		/*
		ctx.submit(EntityRenderTypes.texture(texture, translucent), (pose, buffer) -> {
			float w2 = width / 2F;
			buffer.addVertex(pose, -w2, y, 0F).setUv(uv.u1(), uv.v1()).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
			buffer.addVertex(pose, -w2, 0F, 0F).setUv(uv.u1(), uv.v0()).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
			buffer.addVertex(pose, w2, 0F, 0F).setUv(uv.u0(), uv.v0()).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
			buffer.addVertex(pose, w2, y, 0F).setUv(uv.u0(), uv.v1()).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
		});
		 */

		/*
		float delta = ctx.delta();

		var font = ctx.frame().mc().font;

		var cachedData = prop.cachedData;

		if (cachedData == null) {
			cachedData = new CachedTextData(font.split(prop.getText(), prop.wrap).toArray(new FormattedCharSequence[0]));
			prop.cachedData = cachedData;

			for (int i = 0; i < cachedData.lines.length; i++) {
				cachedData.width[i] = font.width(cachedData.lines[i]);
				cachedData.totalWidth = Math.max(cachedData.totalWidth, cachedData.width[i]);
			}
		}

		if (cachedData.lines.length == 0) {
			return;
		}

		float height = prop.lineHeight * cachedData.lines.length;
		float y = -height;

		if (!prop.shadow) {
			y++;
		}

		float scale = 1F / -height * (float) prop.height;
		prop.width = cachedData.totalWidth * scale;

		var bbMat = new Matrix4f();
		bbMat.scale(scale, scale, scale);
		bbMat.rotateY((float) Math.toRadians(180F - prop.getYaw(delta)));
		bbMat.rotateX((float) Math.toRadians(prop.getPitch(delta)));

		if (prop.centered) {
			bbMat.translate(0F, height / 2F, 0F);
		}

		var matrix4f = ctx.frame().poseStack().last();
		matrix4f.mulPose(bbMat);

		int bgColor = prop.backgroundColor.argb();
		int light = prop.fullBright ? LightCoordsUtil.FULL_BRIGHT : prop.getPackedLight();
		float w2 = cachedData.totalWidth / 2F;
		float off = 0F;

		if (bgColor != 0) {
			var buffer = ctx.frame().buffers().getVertexConsumer(RenderTypes.text(Empty.TEXTURE.texturePath()));
			buffer.addVertex(matrix4f, -w2 - 1F, y - 1F, 0F).setColor(bgColor).setLight(light);
			buffer.addVertex(matrix4f, -w2 - 1F, 1F, 0F).setColor(bgColor).setLight(light);
			buffer.addVertex(matrix4f, w2 + 1F, 1F, 0F).setColor(bgColor).setLight(light);
			buffer.addVertex(matrix4f, w2 + 1F, y - 1F, 0F).setColor(bgColor).setLight(light);
			off = 1F;
		}

		cachedData.va.set(-w2 - off, y - off, 0F).mulPosition(bbMat);
		cachedData.vb.set(-w2 - off, off, 0F).mulPosition(bbMat);
		cachedData.vc.set(w2 + off, off, 0F).mulPosition(bbMat);
		cachedData.vd.set(w2 + off, y - off, 0F).mulPosition(bbMat);

		cachedData.box = new AABB(
			Math.min(Math.min(cachedData.va.x, cachedData.vb.x), Math.min(cachedData.vc.x, cachedData.vd.x)) - 0.05F,
			Math.min(Math.min(cachedData.va.y, cachedData.vb.y), Math.min(cachedData.vc.y, cachedData.vd.y)) - 0.05F,
			Math.min(Math.min(cachedData.va.z, cachedData.vb.z), Math.min(cachedData.vc.z, cachedData.vd.z)) - 0.05F,
			Math.max(Math.max(cachedData.va.x, cachedData.vb.x), Math.max(cachedData.vc.x, cachedData.vd.x)) + 0.05F,
			Math.max(Math.max(cachedData.va.y, cachedData.vb.y), Math.max(cachedData.vc.y, cachedData.vd.y)) + 0.05F,
			Math.max(Math.max(cachedData.va.z, cachedData.vb.z), Math.max(cachedData.vc.z, cachedData.vd.z)) + 0.05F
		);

		int color = prop.color.argb();
		var displayMode = prop.seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET;

		for (int i = 0; i < cachedData.lines.length; i++) {
			var pose = ctx.frame().poseStack().last();
			var prepared = font.prepareText(cachedData.lines[i], -cachedData.width[i] / 2F, y, color, prop.shadow && prop.shadowDistance == 0F, false, 0);
			prepared.visit(new Font.GlyphVisitor() {
				@Override
				public void acceptRenderable(TextRenderable renderable) {
					draw(ctx.frame().buffers().getVertexConsumer(renderable.renderType(displayMode)), pose.pose(), renderable, light);
				}
			});

			if (prop.shadowDistance != 0F) {
				matrix4f.translate(0F, 0F, -prop.shadowDistance);

				var shadowPose = ctx.frame().poseStack().last();
				var shadowPrepared = font.prepareText(cachedData.lines[i], (-cachedData.width[i] / 2F) + 1F, y + 1F, ARGB.scaleRGB(color, 0.25F), false, false, 0);
				shadowPrepared.visit(new Font.GlyphVisitor() {
					@Override
					public void acceptRenderable(TextRenderable renderable) {
						renderable.render(shadowPose.pose(), ctx.frame().buffers().getVertexConsumer(renderable.renderType(displayMode)), renderable, light, false);
					}
				});

				matrix4f.translate(0F, 0F, prop.shadowDistance);
			}

			y += prop.lineHeight;
		}
		 */
	}
}
