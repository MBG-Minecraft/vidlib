package dev.latvian.mods.vidlib.feature.prop.builtin;

import dev.latvian.mods.klib.render.DebugRenderTypes;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.bloom.BloomRenderTypes;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import net.neoforged.api.distmarker.Dist;

public class ShapePropRenderer implements PropRenderer<ShapeProp> {
	@AutoRegister(Dist.CLIENT)
	public static final Holder HOLDER = new Holder(ShapeProp.TYPE, new ShapePropRenderer());

	@Override
	public void render(PropRenderContext<ShapeProp> ctx) {
		var ms = ctx.poseStack();
		var buffers = ctx.frame().buffers();
		var prop = ctx.prop();
		var progress = prop.getRelativeTick(ctx.delta(), 1F);
		float w = (float) prop.width;
		float h = (float) prop.height;

		if (w != 1F || h != 1F) {
			ms.scale(w, h, w);
		}

		ms.translate(0F, 0.5F, 0F);

		var lc = prop.outlineColor.get(progress);

		if (lc.alpha() > 0) {
			if (prop.bloom) {
				prop.shape.buildQuads(0F, 0F, 0F, ms.last().transform(BloomRenderTypes.POS_COL_BUFFER_SUPPLIER.quadsCull(BloomRenderTypes.overridePosCol(buffers))).withColor(lc));
			} else {
				prop.shape.buildLines(0F, 0F, 0F, ms.last().transform(buffers.getBuffer(DebugRenderTypes.LINES)).withColor(lc));
			}
		}

		var c = prop.color.get(progress);

		if (c.alpha() > 0) {
			if (prop.bloom) {
				prop.shape.buildQuads(0F, 0F, 0F, ms.last().transform(buffers.getBuffer(DebugRenderTypes.QUADS)).withColor(c));
			} else {
				prop.shape.buildQuads(0F, 0F, 0F, ms.last().transform(buffers.getBuffer(DebugRenderTypes.QUADS_NO_DEPTH)).withColor(c.withAlpha(50)));
			}
		}

	}
}
