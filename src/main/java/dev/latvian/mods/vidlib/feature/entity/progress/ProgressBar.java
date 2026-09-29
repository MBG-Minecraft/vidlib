package dev.latvian.mods.vidlib.feature.entity.progress;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record ProgressBar(ProgressBarType type, ProgressGetter progressGetter) {
	public record Value(ProgressBar bar, float progress) {
	}

	@FunctionalInterface
	public interface ValueSupplier {
		List<Value> getValues(Level level, float delta);
	}

	public static final List<ValueSupplier> SUPPLIERS = new ArrayList<>(0);

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
	public static Function<Player, ProgressBar> PLAYER = p -> DEFAULT_ENTITY;
}
