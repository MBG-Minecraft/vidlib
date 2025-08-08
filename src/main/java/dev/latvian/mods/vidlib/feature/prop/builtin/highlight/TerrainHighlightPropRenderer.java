package dev.latvian.mods.vidlib.feature.prop.builtin.highlight;

import dev.latvian.mods.klib.math.Vec3f;
import dev.latvian.mods.klib.render.DebugRenderTypes;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;

public class TerrainHighlightPropRenderer implements PropRenderer<TerrainHighlightProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = new Holder(TerrainHighlightProp.TYPE, new TerrainHighlightPropRenderer());

	@Override
	public void render(PropRenderContext<TerrainHighlightProp> ctx) {
		var ms = ctx.poseStack();
		var prop = ctx.prop();

		var s = prop.prevRenderScale.lerp(ctx.delta(), prop.renderScale);

		if (s != Vec3f.ONE) {
			ms.scale(s.x(), s.y(), s.z());
		}

		var buffer = ctx.frame().buffers().getBuffer(DebugRenderTypes.QUADS_NO_CULL_NO_DEPTH).onlyPos();
		prop.shape.buildQuads(0F, 0F, 0F, ms.last().transform(buffer).withColor(prop.color.get(prop.getRelativeTick(ctx.delta(), 0F))));
	}
}
