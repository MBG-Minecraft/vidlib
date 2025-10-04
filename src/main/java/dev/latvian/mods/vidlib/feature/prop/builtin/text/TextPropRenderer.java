package dev.latvian.mods.vidlib.feature.prop.builtin.text;

import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FormattedCharSequence;

public class TextPropRenderer implements PropRenderer<TextProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = new Holder(TextProp.TYPE, new TextPropRenderer());

	@Override
	public void render(PropRenderContext<TextProp> ctx) {
		var prop = ctx.prop();
		float delta = ctx.delta();

		var font = ctx.frame().mc().font;

		if (prop.cachedText == null) {
			prop.cachedText = new CachedTextData(font.split(prop.getText(), prop.wrap).toArray(new FormattedCharSequence[0]));

			for (int i = 0; i < prop.cachedText.lines.length; i++) {
				prop.cachedText.width[i] = font.width(prop.cachedText.lines[i]);
				prop.cachedText.totalWidth = Math.max(prop.cachedText.totalWidth, prop.cachedText.width[i]);
			}
		}

		if (prop.cachedText.lines.length == 0) {
			return;
		}

		int height = font.lineHeight * prop.cachedText.lines.length;
		int y = -height;

		if (!prop.shadow) {
			y++;
		}

		float scale = 1F / -height * (float) prop.height;
		prop.width = prop.cachedText.totalWidth * scale;

		var matrix4f = ctx.frame().poseStack().last().pose();
		matrix4f.scale(scale, scale, scale);
		matrix4f.rotateY((float) Math.toRadians(180F - prop.getYaw(delta)));
		matrix4f.rotateX((float) Math.toRadians(prop.getPitch(delta)));

		int bgColor = prop.backgroundColor.argb();
		int light = LightTexture.FULL_BRIGHT;

		if (bgColor != 0) {
			float w = prop.cachedText.totalWidth / 2F;
			int h = -y;
			var buffer = ctx.frame().buffers().getBuffer(prop.seeThrough ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
			buffer.addVertex(matrix4f, -w - 1F, -h - 1F, 0F).setColor(bgColor).setLight(light);
			buffer.addVertex(matrix4f, -w - 1F, 1F, 0F).setColor(bgColor).setLight(light);
			buffer.addVertex(matrix4f, w + 1F, 1F, 0F).setColor(bgColor).setLight(light);
			buffer.addVertex(matrix4f, w + 1F, -h - 1F, 0F).setColor(bgColor).setLight(light);
		}

		int color = prop.color.argb();

		for (int i = 0; i < prop.cachedText.lines.length; i++) {
			font.drawInBatch(
				prop.cachedText.lines[i],
				-prop.cachedText.width[i] / 2F,
				y,
				color,
				prop.shadow,
				matrix4f,
				ctx.frame().buffers(),
				prop.seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
				0,
				light
			);

			y += font.lineHeight;
		}
	}
}
