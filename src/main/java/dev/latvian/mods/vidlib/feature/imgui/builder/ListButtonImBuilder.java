package dev.latvian.mods.vidlib.feature.imgui.builder;

public interface ListButtonImBuilder {
	default void enableListItemButtons(int index) {
	}

	ListItemAction getListItemAction();
}
