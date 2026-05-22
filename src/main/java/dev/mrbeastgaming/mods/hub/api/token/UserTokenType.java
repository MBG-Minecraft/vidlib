package dev.mrbeastgaming.mods.hub.api.token;

public enum UserTokenType {
	UNKNOWN,
	WEB,
	DESKTOP,
	BOT,

	;

	public static final UserTokenType[] VALUES = values();

	public static UserTokenType get(int index) {
		return index < 0 || index >= VALUES.length ? UNKNOWN : VALUES[index];
	}
}
