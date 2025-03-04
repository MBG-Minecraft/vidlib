package dev.beast.mods.shimmer.feature.session;

public interface Cookie {
	CookieType<?> type();

	default boolean needsSync() {
		return false;
	}
}
