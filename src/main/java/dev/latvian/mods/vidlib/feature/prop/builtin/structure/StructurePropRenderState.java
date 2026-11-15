package dev.latvian.mods.vidlib.feature.prop.builtin.structure;

import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.vidlib.feature.client.VidLibExtractContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import dev.latvian.mods.vidlib.feature.structure.StructureRenderer;
import dev.latvian.mods.vidlib.feature.structure.StructureRendererData;
import net.minecraft.core.BlockPos;
import org.joml.Quaternionf;

public class StructurePropRenderState extends PropRenderState<StructureProp> {
	public final Quaternionf rotate = new Quaternionf();
	public StructureRenderer renderer;
	public StructureRendererData data;

	@Override
	public void extract(StructureProp prop, VidLibExtractContext ctx) {
		renderer = prop.getRenderer();
		data = renderer == null ? null : prop.createRendererData();

		rotate.identity();
		rotate.rotateY((float) Math.toRadians(-prop.getYaw(ctx.delta())));
		rotate.rotateX((float) Math.toRadians(prop.getPitch(ctx.delta())));
		rotate.rotateZ((float) Math.toRadians(prop.getRoll(ctx.delta())));

		if (renderer != null) {
			renderer.origin = BlockPos.containing(x, y, z);
			renderer.preRender(data);
		}
	}

	@Override
	public void submit(SubmitContext ctx) {
		if (renderer == null || data == null) {
			return;
		}

		ctx.rotate(rotate);
		renderer.submit(ctx, data);
	}
}