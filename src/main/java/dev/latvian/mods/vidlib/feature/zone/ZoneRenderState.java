package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.klib.render.Submittable;
import dev.latvian.mods.klib.volume.EmptyVolume;
import dev.latvian.mods.klib.volume.UniverseVolume;
import dev.latvian.mods.klib.volume.Volume;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.jetbrains.annotations.Nullable;

public class ZoneRenderState implements Submittable {
	public Volume volume;
	public Color quadColor;
	public Color lineColor;
	public Color outerLineColor;

	@Nullable
	public static ZoneRenderState extract(ResolvedZone zone, boolean outerLines) {
		if (zone.volume == UniverseVolume.INSTANCE || zone.volume == EmptyVolume.INSTANCE) {
			return null;
		}

		var state = new ZoneRenderState();
		state.volume = zone.volume;
		state.quadColor = zone.color.withAlpha(50);
		state.lineColor = zone.color;
		state.outerLineColor = outerLines ? zone.color : Color.TRANSPARENT;
		return state;
	}

	@Override
	public void submit(SubmitContext ctx) {
		if (!quadColor.isTransparent()) {
			ctx.submit(RenderTypes.debugQuads(), (pose, buffer) -> volume.buildQuads(pose.transform(buffer).withColor(quadColor), ctx.cameraPos()));
		}

		if (!lineColor.isTransparent()) {
			ctx.submit(RenderTypes.linesTranslucentNoDepthWrite(), (pose, buffer) -> volume.buildLines(pose.transform(buffer).withColor(lineColor).withLineWidth(ctx.lineWidth()), ctx.cameraPos()));
		}

		if (!outerLineColor.isTransparent()) {
			ctx.submit(RenderTypes.linesTranslucentNoDepthWrite(), (pose, buffer) -> volume.buildOuterLines(pose.transform(buffer).withColor(outerLineColor).withLineWidth(ctx.lineWidth()), ctx.cameraPos()));
		}
	}
}
