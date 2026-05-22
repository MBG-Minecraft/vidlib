package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class CollectionImBuilder<T, C extends Collection<T>> implements ImBuilder<C> {
	public final ImBuilderType<T> type;
	public final List<ImBuilder<T>> items;

	public CollectionImBuilder(ImBuilderType<T> elementType) {
		this.type = elementType;
		this.items = new ArrayList<>();
	}

	@Override
	public void set(C value) {
		if (value == null || value.isEmpty()) {
			items.clear();
			return;
		}

		int newSize = value.size();
		while (items.size() > newSize) {
			items.removeLast();
		}

		// Reuse existing builders and add new ones as needed
		int i = 0;
		for (var item : value) {
			if (i < items.size()) {
				items.get(i).set(item);
			} else {
				var builder = type.get();
				builder.set(item);
				items.add(builder);
			}
			i++;
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		int delete = -1;
		int moveUp = -1;
		int moveDown = -1;

		// This is so that two different ListImBuilders don't interfere with each other
		ImGui.pushID(System.identityHashCode(this));

		for (int i = 0; i < items.size(); i++) {
			ImGui.pushID(i);
			var item = items.get(i);

			if (item instanceof ListButtonImBuilder lb) {
				lb.enableListItemButtons(i);
			}

			if (item.isSmall()) {
				update = update.or(item.imgui(graphics));
				ImGui.sameLine();
			}

			if (!(item instanceof ListButtonImBuilder)) {
				graphics.redTextIf("#" + (i + 1), !item.isValid());
				ImGui.sameLine();
				ImGui.dummy(1, 0);
				ImGui.sameLine();
				graphics.pushStack();
				graphics.setRedButton();

				if (ImGui.smallButton(ImIcons.TRASHCAN + " Delete###delete-item")) {
					delete = i;
					update = ImUpdate.FULL;
				}

				graphics.popStack();
			}

			if (!item.isSmall()) {
				update = update.or(item.imgui(graphics));
			}

			if (item instanceof ListButtonImBuilder lb) {
				switch (lb.getListItemAction()) {
					case DELETE -> {
						delete = i;
						update = ImUpdate.FULL;
					}
					case MOVE_UP -> {
						moveUp = i;
						update = ImUpdate.FULL;
					}
					case MOVE_DOWN -> {
						moveDown = i;
						update = ImUpdate.FULL;
					}
				}
			}

			ImGui.popID();
			ImGui.spacing();
		}

		if (delete != -1) {
			items.remove(delete);
		} else if (moveUp != -1) {
			if (moveUp > 0) {
				items.add(moveUp - 1, items.remove(moveUp));
			}
		} else if (moveDown != -1) {
			if (moveDown < items.size() - 1) {
				items.add(moveDown + 1, items.remove(moveDown));
			}
		}


		if (ImGui.button(ImIcons.ADD + " Add ###add-item")) {
			var builder = type.get();
			builder.set(builder.build());
			items.add(builder);
			update = ImUpdate.FULL;
		}

		ImGui.popID();
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

	public abstract C createCollection(int size);

	@Override
	public C build() {
		var list = createCollection(items.size());

		for (var builder : items) {
			list.add(builder.build());
		}

		return list;
	}
}
