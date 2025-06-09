package dev.latvian.mods.vidlib.feature.prop.builtin;

import dev.latvian.mods.klib.render.BufferSupplier;
import dev.latvian.mods.klib.render.CuboidRenderer;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.bloom.BloomRenderTypes;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import dev.latvian.mods.vidlib.util.client.FrameInfo;
import net.neoforged.api.distmarker.Dist;

public class BrightCubePropRenderer implements PropRenderer<BrightCubeProp> {
	@AutoRegister(Dist.CLIENT)
	public static final Holder HOLDER = new Holder(BrightCubeProp.TYPE, new BrightCubePropRenderer());

	@Override
	public void renderProp(BrightCubeProp prop, FrameInfo frame) {
		float w = (float) (prop.width / 2D);
		float h = (float) prop.height;

		if (prop.outlineColor.alpha() > 0) {
			CuboidRenderer.quads(frame.poseStack(), -w, 0F, -w, w, h, w, BloomRenderTypes.overridePosCol(frame.buffers()), BloomRenderTypes.POS_COL_BUFFER_SUPPLIER, true, prop.outlineColor);
		}

		if (prop.color.alpha() > 0) {
			CuboidRenderer.quads(frame.poseStack(), -w, 0F, -w, w, h, w, frame.buffers(), BufferSupplier.DEBUG, true, prop.color);
		}
	}
}
