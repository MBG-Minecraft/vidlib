package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;

public enum GameTimeKNumber implements KNumber {
	INSTANCE;

	public static final SimpleRegistryType<GameTimeKNumber> TYPE = SimpleRegistryType.unit("game_time", INSTANCE);

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public Double get(KNumberContext ctx) {
		return (double) ctx.level.getGameTime();
	}
}
