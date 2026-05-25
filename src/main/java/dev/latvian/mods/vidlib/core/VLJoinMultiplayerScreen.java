package dev.latvian.mods.vidlib.core;

import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;

import java.util.Collection;
import java.util.Set;

public interface VLJoinMultiplayerScreen {
	default void vl$refresh() {
	}

	default Collection<ServerSelectionList.Entry> vl$hubServers() {
		return Set.of();
	}
}
