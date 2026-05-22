package dev.latvian.mods.vidlib.feature.prop.builtin.text;

import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ARGB;
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

		var matrix4f = ctx.frame().poseStack().last().pose();
		matrix4f.mul(bbMat);

		int bgColor = prop.backgroundColor.argb();
		int light = prop.fullBright ? LightTexture.FULL_BRIGHT : prop.getPackedLight();
		float w2 = cachedData.totalWidth / 2F;
		float off = 0F;

		if (bgColor != 0) {
			var buffer = ctx.frame().buffers().getBuffer(prop.seeThrough ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
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

		// new AABB(-width / 2D, 0D, -width / 2D, width / 2D, height, width / 2D)

		int color = prop.color.argb();

		for (int i = 0; i < cachedData.lines.length; i++) {
			font.drawInBatch(
				cachedData.lines[i],
				-cachedData.width[i] / 2F,
				y,
				color,
				prop.shadow && prop.shadowDistance == 0F,
				matrix4f,
				ctx.frame().buffers(),
				prop.seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
				0,
				light
			);

			if (prop.shadowDistance != 0F) {
				matrix4f.translate(0F, 0F, -prop.shadowDistance);

				font.drawInBatch(
					cachedData.lines[i],
					(-cachedData.width[i] / 2F) + 1F,
					y + 1F,
					ARGB.scaleRGB(color, 0.25F),
					false,
					matrix4f,
					ctx.frame().buffers(),
					prop.seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
					0,
					light
				);

				matrix4f.translate(0F, 0F, prop.shadowDistance);
			}

			y += prop.lineHeight;
		}
	}
}
