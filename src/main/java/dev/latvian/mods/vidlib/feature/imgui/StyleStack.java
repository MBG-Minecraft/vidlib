package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for styling {@link ImGui}, modeled after {@link com.mojang.blaze3d.vertex.PoseStack}.
 */
public class StyleStack {

	private final List<Entry> entries = new ArrayList<>();

	/**
	 * Push a new layer of transformations to this {@link StyleStack}.
	 */
	public void push() {
		entries.add(new Entry());
	}

	/**
	 * Revert the previous layer of transformations to this {@link StyleStack}.
	 */
	public void pop() {
		if (entries.isEmpty()) {
			return;
		}

		Entry entry = entries.remove(entries.size() - 1);
		entry.clear();
	}

	public void color(int imGuiCol, float r, float g, float b, float a) {
		ImGui.pushStyleColor(imGuiCol, r, g, b, a);
		peek().color();
	}

	public void color(int imGuiCol, int r, int g, int b, int a) {
		ImGui.pushStyleColor(imGuiCol, r, g, b, a);
		peek().color();
	}

	public void color(int imGuiCol, int col) {
		ImGui.pushStyleColor(imGuiCol, col);
		peek().color();
	}

	public void var(int imGuiStyleVar, float val) {
		ImGui.pushStyleVar(imGuiStyleVar, val);
		peek().var();
	}

	public void var(int imGuiStyleVar, float valX, float valY) {
		ImGui.pushStyleVar(imGuiStyleVar, valX, valY);
		peek().var();
	}

	private Entry peek() {
		return entries.get(entries.size() - 1);
	}

	private static class Entry {

		private final List<Type> entries = new ArrayList<>();

		public void clear() {
			for (Type entry : entries) {
				if (entry.color) {
					ImGui.popStyleColor();
				} else {
					ImGui.popStyleVar();
				}
			}
		}

		public void color() {
			entries.add(new Type(true));
		}

		public void var() {
			entries.add(new Type(false));
		}
	}

	private record Type(boolean color) {

	}
}
