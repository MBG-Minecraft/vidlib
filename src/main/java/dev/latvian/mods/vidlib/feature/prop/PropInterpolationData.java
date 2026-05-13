package dev.latvian.mods.vidlib.feature.prop;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

public interface PropInterpolationData {
	@Nullable
	Number value(PropData<?, ?> key);

	@Nullable
	Vector3dc position(PropData<?, ?> key);

	@Nullable
	Number degrees(PropData<?, ?> key);
}
