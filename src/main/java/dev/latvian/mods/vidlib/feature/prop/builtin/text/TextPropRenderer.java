package dev.latvian.mods.vidlib.feature.prop.builtin.text;

import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class TextPropRenderer implements PropRenderer<TextProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = new Holder(TextProp.TYPE, new TextPropRenderer());

	@Override
	public void render(PropRenderContext<TextProp> ctx) {
		var prop = ctx.prop();
		float delta = ctx.delta();

		var font = ctx.frame().mc().font;

		if (prop.cachedData == null) {
			prop.cachedData = new CachedTextData(font.split(prop.getText(), prop.wrap).toArray(new FormattedCharSequence[0]));

			for (int i = 0; i < prop.cachedData.lines.length; i++) {
				prop.cachedData.width[i] = font.width(prop.cachedData.lines[i]);
				prop.cachedData.totalWidth = Math.max(prop.cachedData.totalWidth, prop.cachedData.width[i]);
			}
		}

		if (prop.cachedData.lines.length == 0) {
			return;
		}

		float height = prop.lineHeight * prop.cachedData.lines.length;
		float y = -height;

		if (!prop.shadow) {
			y++;
		}

		float scale = 1F / -height * (float) prop.height;
		prop.width = prop.cachedData.totalWidth * scale;

		var bbMat = new Matrix4f();
		bbMat.scale(scale, scale, scale);
		bbMat.rotateY((float) Math.toRadians(180F - prop.getYaw(delta)));
		bbMat.rotateX((float) Math.toRadians(prop.getPitch(delta)));

		if (prop.centered) {
			bbMat.translate(0F, height / 2F, 0F);
		}

		var matrix4f = ctx.frame().poseStack().last().pose();
		matrix4f.mul(bbMat);

		int bgColor = prop.backgroundColor.argb();
		int light = LightTexture.FULL_BRIGHT;
		float w2 = prop.cachedData.totalWidth / 2F;
		float off = 0F;

		if (bgColor != 0) {
			var buffer = ctx.frame().buffers().getBuffer(prop.seeThrough ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
			buffer.addVertex(matrix4f, -w2 - 1F, y - 1F, 0F).setColor(bgColor).setLight(light);
			buffer.addVertex(matrix4f, -w2 - 1F, 1F, 0F).setColor(bgColor).setLight(light);
			buffer.addVertex(matrix4f, w2 + 1F, 1F, 0F).setColor(bgColor).setLight(light);
			buffer.addVertex(matrix4f, w2 + 1F, y - 1F, 0F).setColor(bgColor).setLight(light);
			off = 1F;
		}

		prop.cachedData.va.set(-w2 - off, y - off, 0F).mulPosition(bbMat);
		prop.cachedData.vb.set(-w2 - off, off, 0F).mulPosition(bbMat);
		prop.cachedData.vc.set(w2 + off, off, 0F).mulPosition(bbMat);
		prop.cachedData.vd.set(w2 + off, y - off, 0F).mulPosition(bbMat);

		prop.cachedData.box = new AABB(
			Math.min(Math.min(prop.cachedData.va.x, prop.cachedData.vb.x), Math.min(prop.cachedData.vc.x, prop.cachedData.vd.x)) - 0.05F,
			Math.min(Math.min(prop.cachedData.va.y, prop.cachedData.vb.y), Math.min(prop.cachedData.vc.y, prop.cachedData.vd.y)) - 0.05F,
			Math.min(Math.min(prop.cachedData.va.z, prop.cachedData.vb.z), Math.min(prop.cachedData.vc.z, prop.cachedData.vd.z)) - 0.05F,
			Math.max(Math.max(prop.cachedData.va.x, prop.cachedData.vb.x), Math.max(prop.cachedData.vc.x, prop.cachedData.vd.x)) + 0.05F,
			Math.max(Math.max(prop.cachedData.va.y, prop.cachedData.vb.y), Math.max(prop.cachedData.vc.y, prop.cachedData.vd.y)) + 0.05F,
			Math.max(Math.max(prop.cachedData.va.z, prop.cachedData.vb.z), Math.max(prop.cachedData.vc.z, prop.cachedData.vd.z)) + 0.05F
		);

		// new AABB(-width / 2D, 0D, -width / 2D, width / 2D, height, width / 2D)

		int color = prop.color.argb();

		for (int i = 0; i < prop.cachedData.lines.length; i++) {
			font.drawInBatch(
				prop.cachedData.lines[i],
				-prop.cachedData.width[i] / 2F,
				y,
				color,
				prop.shadow,
				matrix4f,
				ctx.frame().buffers(),
				prop.seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
				0,
				light
			);

			y += prop.lineHeight;
		}
	}
}
