package dev.latvian.mods.vidlib.feature.imui;

public interface IMUI {
	void pushID(int id);

	default void pushID(String id) {
		pushID(id.hashCode());
	}

	void popID();

	void text(String text);
}
