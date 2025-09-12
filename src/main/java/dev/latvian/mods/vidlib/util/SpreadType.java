package dev.latvian.mods.vidlib.util;

import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.Vec2f;
import net.minecraft.util.Mth;

public enum SpreadType {
	CIRCLE,
	FILLED_CIRCLE,
	SQUARE,
	FILLED_SQUARE,
	LINE;

	public static final SpreadType[] VALUES = values();
	public static final DataType<SpreadType> DATA_TYPE = DataType.of(VALUES);

	public Vec2f offset(int index, int count, float radius) {
		float delta = index / (float) count;

		if (this == CIRCLE) {
			float angle = delta * Mth.TWO_PI;
			return new Vec2f(Mth.sin(angle) * radius, Mth.cos(angle) * radius);
		} else if (this == FILLED_CIRCLE) {
			float angle = delta * Mth.TWO_PI;
			float currentRadius = radius * Mth.sqrt(delta);
			return new Vec2f(currentRadius * Mth.cos(angle), currentRadius * Mth.sin(angle));
		} else if (this == SQUARE) {
			if (delta < 0.25F) {
				return new Vec2f(Mth.lerp(delta * 4F, -radius, radius), -radius);
			} else if (delta < 0.5F) {
				return new Vec2f(radius, Mth.lerp((delta - 0.25F) * 4F, -radius, radius));
			} else if (delta < 0.75F) {
				return new Vec2f(Mth.lerp((delta - 0.5F) * 4F, radius, -radius), radius);
			} else {
				return new Vec2f(-radius, Mth.lerp((delta - 0.75F) * 4F, radius, -radius));
			}
		} else if (this == FILLED_SQUARE) {
			int max = Mth.ceil(Mth.sqrt(count));
			int x = index % max;
			int y = index / max;

			return new Vec2f(
				Mth.lerp((x + 0.5F) / (float) max, -radius, radius),
				Mth.lerp((y + 0.5F) / (float) max, -radius, radius)
			);
		} else {
			return new Vec2f(Mth.lerp((index + 0.5F) / (float) count, -radius, radius), 0F);
		}
	}
}
