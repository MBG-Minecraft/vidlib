package dev.latvian.mods.userapi;

import dev.latvian.mods.klib.color.Color;

import java.util.Set;

public record User(
	API api,
	int id,
	String name,
	Color color,
	Set<String> roles
) {
	@Override
	public String toString() {
		return "User[%06X %s]".formatted(id, name);
	}

	public boolean isUnknown() {
		return id <= 0;
	}
}
