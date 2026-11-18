package dev.latvian.mods.vidlib.feature.prop.builtin.structure;

import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.VidLibExtractContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import dev.latvian.mods.vidlib.feature.structure.StructureRenderer;
import dev.latvian.mods.vidlib.feature.structure.StructureRendererData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Map;

public class StructurePropRenderState extends PropRenderState<StructureProp> {
	private static final Map<Identifier, StructureRenderer> RENDERERS = new HashMap<>();

	@Nullable
	public static StructureRenderer getRenderer(@Nullable Identifier structureId) {
		if (structureId == null || structureId.equals(ID.EMPTY)) {
			return null;
		}

		return RENDERERS.computeIfAbsent(structureId, id -> StructureRenderer.create(id, id));
	}

	public final Quaternionf rotate = new Quaternionf();
	public StructureRenderer renderer;
	public StructureRendererData data;

	@Override
	public void extract(StructureProp prop, VidLibExtractContext ctx) {
		renderer = getRenderer(prop.structure);
		data = renderer == null ? null : prop.createRendererData();

		rotate.identity();
		rotate.rotateY((float) Math.toRadians(-prop.getYaw(ctx.delta())));
		rotate.rotateX((float) Math.toRadians(prop.getPitch(ctx.delta())));
		rotate.rotateZ((float) Math.toRadians(prop.getRoll(ctx.delta())));

		if (renderer != null) {
			renderer.origin = BlockPos.containing(x, y, z);
			renderer.preRender(data);
		}

		if (renderer == null || data == null) {
			invisible = true;
		}
	}

	@Override
	public void submit(SubmitContext ctx) {
		ctx.rotate(rotate);
		renderer.submit(ctx, data);
	}
}