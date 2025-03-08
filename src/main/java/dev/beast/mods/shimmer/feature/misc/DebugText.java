package dev.beast.mods.shimmer.feature.misc;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class DebugText {
	public static class DebugTextList {
		public final List<Component> list = new ArrayList<>(0);

		public void add(Component component) {
			list.add(component);
		}

		public void add(String text) {
			list.add(Component.literal(text));
		}
	}

	public static final DebugText RENDER = new DebugText();
	public static final DebugText CLIENT_TICK = new DebugText();

	public final DebugTextList topLeft = new DebugTextList();
	public final DebugTextList topRight = new DebugTextList();
	public final DebugTextList bottomLeft = new DebugTextList();
	public final DebugTextList bottomRight = new DebugTextList();

	public void clear() {
		topLeft.list.clear();
		topRight.list.clear();
		bottomLeft.list.clear();
		bottomRight.list.clear();
	}

	public void addAll(DebugText from) {
		topLeft.list.addAll(from.topLeft.list);
		topRight.list.addAll(from.topRight.list);
		bottomLeft.list.addAll(from.bottomLeft.list);
		bottomRight.list.addAll(from.bottomRight.list);
	}
}
