package dev.latvian.mods.vidlib.util;

import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.Vec2f;
import net.minecraft.util.Mth;

import java.util.ArrayList;

public enum SpreadType {
	CIRCLE,
	FILLED_CIRCLE,
	SQUARE,
	FILLED_SQUARE,
	LINE;

	public static final SpreadType[] VALUES = values();
	public static final DataType<SpreadType> DATA_TYPE = DataType.of(VALUES);

	public Vec2f[] spread(int count) {
		return switch (this) {
			case CIRCLE -> {
				var values = new Vec2f[count];

				for (int i = 0; i < count; i++) {
					float angle = i / (float) count * Mth.TWO_PI;
					values[i] = new Vec2f(Mth.cos(angle), Mth.sin(angle));
				}

				yield values;
			}
			case FILLED_CIRCLE -> {
				var values = new ArrayList<Vec2f>(count);
				double cr = Math.sqrt(count / Math.PI);
				int r = Mth.ceil(cr);

				for (int y = -r; y <= r; y++) {
					for (int x = -r; x <= r; x++) {
						if (x * x + y * y <= cr * cr) {
							values.add(new Vec2f(x / (float) r, y / (float) r));
						}
					}
				}

				yield values.toArray(new Vec2f[0]);
			}
			case SQUARE -> {
				var values = new Vec2f[count];

				for (int i = 0; i < count; i++) {
					float delta = (i + 0.5F) / (float) count;

					if (delta < 0.25F) {
						values[i] = new Vec2f(Mth.lerp(delta * 4F, -1F, 1F), -1F);
					} else if (delta < 0.5F) {
						values[i] = new Vec2f(1F, Mth.lerp((delta - 0.25F) * 4F, -1F, 1F));
					} else if (delta < 0.75F) {
						values[i] = new Vec2f(Mth.lerp((delta - 0.5F) * 4F, 1F, -1F), 1F);
					} else {
						values[i] = new Vec2f(-1F, Mth.lerp((delta - 0.75F) * 4F, 1F, -1F));
					}
				}

				yield values;
			}
			case FILLED_SQUARE -> {
				int max = Mth.ceil(Mth.sqrt(count));
				var values = new Vec2f[count];

				for (int i = 0; i < count; i++) {
					int x = i % max;
					int y = i / max;

					values[i] = new Vec2f(
						Mth.lerp((x + 0.5F) / (float) max, -1F, 1F),
						Mth.lerp((y + 0.5F) / (float) max, -1F, 1F)
					);
				}

				yield values;
			}
			case LINE -> {
				var values = new Vec2f[count];

				for (int i = 0; i < count; i++) {
					values[i] = new Vec2f(Mth.lerp((i + 0.5F) / (float) count, -1F, 1F), 0F);
				}

				yield values;
			}
		};
	}
}
