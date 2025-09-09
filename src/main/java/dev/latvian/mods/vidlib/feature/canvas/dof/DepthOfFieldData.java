package dev.latvian.mods.vidlib.feature.canvas.dof;

import dev.latvian.mods.vidlib.math.kvector.KVector;

public record DepthOfFieldData(KVector focus, float focusRange, float blurRange, float strength, DepthOfFieldShape shape) {
	public DepthOfFieldData withFocus(KVector focus) {
		return new DepthOfFieldData(focus, focusRange, blurRange, strength, shape);
	}
}

