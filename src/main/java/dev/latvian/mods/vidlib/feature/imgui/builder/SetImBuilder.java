package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetImBuilder<T> implements ImBuilder<Set<T>> {
	public final ImBuilderType<T> type;
	public final List<ImBuilder<T>> items;

	public SetImBuilder(ImBuilderType<T> elementType) {
		this.type = elementType;
		this.items = new ArrayList<>();
	}

	@Override
	public void set(Set<T> value) {
		items.clear();

		if (value != null && !value.isEmpty()) {
			for (var item : value) {
				var builder = type.get();
				builder.set(item);
				items.add(builder);
			}
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		int delete = -1;

		for (int i = 0; i < items.size(); i++) {
			ImGui.pushID(i);
			var item = items.get(i);
			graphics.redTextIf("#" + (i + 1), !item.isValid());
			ImGui.sameLine();
			graphics.pushStack();
			graphics.setRedButton();

			if (ImGui.smallButton(ImIcons.TRASHCAN + " Delete###delete-item")) {
				delete = i;
				update = ImUpdate.FULL;
			}

			graphics.popStack();

			update = update.or(item.imgui(graphics));
			ImGui.popID();
		}

		if (delete != -1) {
			items.remove(delete);
		}

		if (ImGui.button(ImIcons.ADD + " Add###add-item")) {
			var builder = type.get();
			builder.set(builder.build());
			items.add(builder);
			update = ImUpdate.FULL;
		}

		return update;
	}

	@Override
	public boolean isValid() {
		for (var builder : items) {
			if (!builder.isValid()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public Set<T> build() {
		var set = new HashSet<T>(items.size());

		for (var builder : items) {
			set.add(builder.build());
		}

		return set;
	}
}
