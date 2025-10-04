package dev.latvian.mods.vidlib.feature.prop.builtin.text;

import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;

public class TextPropRenderer implements PropRenderer<TextProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = new Holder(TextProp.TYPE, new TextPropRenderer());

	@Override
	public void render(PropRenderContext<TextProp> ctx) {
		var prop = ctx.prop();
		float delta = ctx.delta();

		var font = ctx.frame().mc().font;

		if (prop.cachedText == null) {
			prop.cachedText = font.split(prop.getText(), prop.wrap);
		}

		if (prop.cachedText.isEmpty()) {
			return;
		}

		int y = -font.lineHeight * prop.cachedText.size();
		float scale = 1F / y * (float) prop.height;

		var matrix4f = ctx.frame().poseStack().last().pose();
		matrix4f.scale(scale, scale, scale);
		matrix4f.rotateY((float) Math.toRadians(180F + prop.getYaw(delta)));
		matrix4f.rotateX((float) Math.toRadians(prop.getPitch(delta)));

		int color = prop.color.argb();
		float totalWidth = 0F;

		for (var line : prop.cachedText) {
			float w = font.width(line);
			totalWidth = Math.max(totalWidth, w);

			font.drawInBatch(
				line,
				-w / 2F,
				y,
				color,
				prop.shadow,
				matrix4f,
				ctx.frame().buffers(),
				prop.seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
				0,
				LightTexture.FULL_BRIGHT
			);

			y += font.lineHeight;
		}

		prop.width = totalWidth * scale;
	}
}
