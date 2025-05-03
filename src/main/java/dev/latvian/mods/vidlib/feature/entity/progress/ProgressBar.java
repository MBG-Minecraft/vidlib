package dev.latvian.mods.vidlib.feature.entity.progress;

import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public record ProgressBar(ProgressBarType type, ProgressGetter progressGetter) {
	public record Value(ProgressBar bar, float progress) {
	}

	@FunctionalInterface
	public interface ValueSupplier {
		ValueSupplier BOSS_ENTITY_BARS = (level, delta) -> {
			var list = new ArrayList<Value>(0);

			for (var entity : level.allEntities()) {
				var bar = entity.getBossBar();

				if (bar != null) {
					list.add(new Value(bar, bar.progressGetter.getProgress(entity, delta)));
				}
			}

			return list;
		};

		List<Value> getValues(Level level, float delta);
	}

	public static final List<ValueSupplier> SUPPLIERS = new ArrayList<>(List.of(ValueSupplier.BOSS_ENTITY_BARS));

	public static ProgressBar entity(ProgressBarType type) {
		return new ProgressBar(type, ProgressGetter.ENTITY_HEALTH);
	}

	public static final ProgressBar BLUE_ENTITY = entity(ProgressBarType.BLUE);
	public static final ProgressBar GREEN_ENTITY = entity(ProgressBarType.GREEN);
	public static final ProgressBar PINK_ENTITY = entity(ProgressBarType.PINK);
	public static final ProgressBar PURPLE_ENTITY = entity(ProgressBarType.PURPLE);
	public static final ProgressBar RED_ENTITY = entity(ProgressBarType.RED);
	public static final ProgressBar WHITE_ENTITY = entity(ProgressBarType.WHITE);
	public static final ProgressBar YELLOW_ENTITY = entity(ProgressBarType.YELLOW);

	public static ProgressBar DEFAULT_ENTITY = PURPLE_ENTITY;
}
