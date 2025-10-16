package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum LiteralKNumber implements KNumber {
	PROGRESS("progress", "Progress", ctx -> ctx.progress),
	TICK("tick", "Tick", ctx -> ctx.tick),
	MAX_TICK("max_tick", "Max Tick", ctx -> ctx.maxTick),
	GAME_TIME("game_time", "Game Time", ctx -> ctx.gameTime),
	GAME_DAY("game_day", "Game Day", ctx -> ctx.gameDay),
	CLOCK("clock", "Clock", ctx -> ctx.clock),

	;

	public static final LiteralKNumber[] VALUES = values();
	public static final Map<String, LiteralKNumber> BY_NAME = Arrays.stream(VALUES).collect(Collectors.toMap(LiteralKNumber::toString, Function.identity()));

	public final String name;
	public final String displayName;
	public final Function<KNumberContext, Double> factory;
	public final SimpleRegistryType.Unit<LiteralKNumber> type;

	LiteralKNumber(String name, String displayName, Function<KNumberContext, Double> factory) {
		this.name = name;
		this.displayName = displayName;
		this.factory = factory;
		this.type = SimpleRegistryType.unit(name, this);
	}

	@Override
	public SimpleRegistryType<?> type() {
		return type;
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
		return factory.apply(ctx);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean isLiteral() {
		return true;
	}
}
