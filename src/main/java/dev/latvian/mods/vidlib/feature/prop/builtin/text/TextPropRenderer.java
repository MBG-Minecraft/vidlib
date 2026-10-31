package dev.latvian.mods.vidlib.feature.prop.builtin.text;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import dev.latvian.mods.vidlib.client.SubmitContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class TextPropRenderer implements PropRenderer<TextProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = PropRenderer.holder(TextProp.TYPE, TextPropRenderer::new);

	@Override
	public void render(SubmitContext<TextProp> ctx) {
		var prop = ctx.prop();
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
						draw(ctx.frame().buffers().getVertexConsumer(renderable.renderType(displayMode)), shadowPose.pose(), renderable, light);
					}
				});

				matrix4f.translate(0F, 0F, prop.shadowDistance);
			}

			y += prop.lineHeight;
		}
	}

	private static void draw(VertexConsumer buffer, org.joml.Matrix4fc pose, TextRenderable renderable, int light) {
		renderable.render(pose, buffer, light, false);
	}
}
