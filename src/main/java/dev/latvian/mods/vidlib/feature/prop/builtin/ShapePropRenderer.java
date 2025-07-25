package dev.latvian.mods.vidlib.feature.prop.builtin;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.render.DebugRenderTypes;
import dev.latvian.mods.klib.texture.LightUV;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.bloom.Bloom;
import dev.latvian.mods.vidlib.feature.bloom.BloomRenderTypes;
import dev.latvian.mods.vidlib.feature.client.TerrainRenderTypes;
import dev.latvian.mods.vidlib.feature.prop.PropHitResult;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import net.minecraft.resources.ResourceLocation;
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
		float sw = (float) prop.width;
		float sh = (float) prop.height;

		if (sw != 1F || sh != 1F) {
			ms.scale(sw, sh, sw);
		}

		ms.translate(0F, 0.5F, 0F);

		var lc = prop.canInteract && ctx.frame().mc().hitResult instanceof PropHitResult hit && hit.prop == prop ? Color.WHITE : prop.outlineColor.get(progress);

		if (lc.alpha() > 0) {
			if (prop.bloom) {
				Bloom.markActive();
				prop.shape.buildQuads(0F, 0F, 0F, ms.last().transform(BloomRenderTypes.POS_COL_BUFFER_SUPPLIER.quadsCull(BloomRenderTypes.overridePosCol(buffers))).withColor(lc));
			} else {
				prop.shape.buildLines(0F, 0F, 0F, ms.last().transform(buffers.getBuffer(DebugRenderTypes.LINES)).withColor(lc));
			}
		}

		var c = prop.color.get(progress);

		if (c.alpha() > 0) {
			if (prop.bloom) {
				Bloom.markActive();
				prop.shape.buildQuads(0F, 0F, 0F, ms.last().transform(buffers.getBuffer(DebugRenderTypes.QUADS)).withColor(c));
			} else {
				var tex = ResourceLocation.withDefaultNamespace("textures/block/birch_log.png");
				var callback = ms.last().transform(buffers.getBuffer(TerrainRenderTypes.SOLID.apply(tex))).withColor(c.withAlpha(50)).withLight(LightUV.FULLBRIGHT);
				prop.shape.buildQuads(0F, 0F, 0F, callback);
			}
		}
	}
}
