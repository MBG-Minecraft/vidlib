package dev.beast.mods.shimmer.feature.misc;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;

import java.util.List;

public class DebugTextEvent extends Event {
	private final List<Component> left;
	private final List<Component> right;

	public DebugTextEvent(List<Component> left, List<Component> right) {
		this.left = left;
		this.right = right;
	}

	public List<Component> getLeft() {
		return left;
	}

	public List<Component> getRight() {
		return right;
	}
}
