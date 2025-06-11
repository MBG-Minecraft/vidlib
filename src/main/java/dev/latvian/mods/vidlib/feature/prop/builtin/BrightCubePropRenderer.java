package dev.latvian.mods.vidlib.feature.prop.builtin;

import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.render.CuboidRenderer;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.bloom.BloomRenderTypes;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import net.neoforged.api.distmarker.Dist;

public class BrightCubePropRenderer implements PropRenderer<BrightCubeProp> {
	@AutoRegister(Dist.CLIENT)
	public static final Holder HOLDER = new Holder(BrightCubeProp.TYPE, new BrightCubePropRenderer());

	@Override
	public void render(PropRenderContext<BrightCubeProp> ctx) {
		var prop = ctx.prop();
		float w = (float) (prop.width / 2D);
		float h = (float) prop.height;

		if (prop.outlineColor.alpha() > 0) {
			CuboidRenderer.quads(ctx.poseStack(), -w, 0F, -w, w, h, w, BloomRenderTypes.overridePosCol(ctx.frame().buffers()), BloomRenderTypes.POS_COL_BUFFER_SUPPLIER, true, prop.outlineColor);
		}

		if (prop.color.alpha() > 0) {
			CuboidRenderer.quads(ctx.poseStack(), -w, 0F, -w, w, h, w, ctx.frame().buffers(), BufferSupplier.DEBUG, true, prop.color);
		}
	}
}
