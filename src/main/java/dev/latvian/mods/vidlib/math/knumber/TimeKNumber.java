package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;

public enum TimeKNumber implements KNumber {
	INSTANCE;

	public static final SimpleRegistryType<TimeKNumber> TYPE = SimpleRegistryType.unit("time", INSTANCE);

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public Double get(KNumberContext ctx) {
		return (double) ((ctx.level.getGameTime() % 240000L) + ctx.level.vl$getDelta()) / 24000D;
	}
}
