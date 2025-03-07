package dev.beast.mods.shimmer.feature.misc;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class DebugTextEvent extends Event {
	public static final List<Component> LEFT = new ArrayList<>();
	public static final List<Component> RIGHT = new ArrayList<>();

	public List<Component> getLeft() {
		return LEFT;
	}

	public List<Component> getRight() {
		return RIGHT;
	}
}
