package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.klib.render.Submittable;
import dev.latvian.mods.klib.volume.EmptyVolume;
import dev.latvian.mods.klib.volume.UniverseVolume;
import dev.latvian.mods.klib.volume.Volume;
import org.jetbrains.annotations.Nullable;

public class ZoneRenderState implements Submittable {
	public Volume volume;
	public int quadColor;
	public int lineColor;
	public int outerLineColor;

	@Nullable
	public static ZoneRenderState extract(ResolvedZone zone, boolean outerLines) {
		if (zone.volume == UniverseVolume.INSTANCE || zone.volume == EmptyVolume.INSTANCE) {
			return null;
		}

		var state = new ZoneRenderState();
		state.volume = zone.volume;
		state.quadColor = zone.color.withAlpha(50).argb();
		state.lineColor = zone.color.argb();
		state.outerLineColor = outerLines ? zone.color.argb() : 0;
		return state;
	}

	@Override
	public void submit(SubmitContext ctx) {
		ctx.submitQuads(volume, quadColor);
		ctx.submitLines(volume, lineColor);
		ctx.submitOuterLines(volume, outerLineColor);
	}
}
