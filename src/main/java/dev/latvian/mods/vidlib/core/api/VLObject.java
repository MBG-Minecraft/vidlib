package dev.latvian.mods.vidlib.core.api;

public interface VLObject {
	default Object vl$get(int id) {
		return null;
	}

	default void vl$set(int id, Object value) {
	}
}
