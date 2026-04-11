package dev.latvian.mods.vidlib.feature.progressqueue;

import dev.latvian.mods.vidlib.feature.imgui.ImText;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ProgressQueue {
	@ApiStatus.Internal
	public static final LinkedList<ProgressQueue> ACTIVE = new LinkedList<>();

	public static ProgressItem queueSingleItem(String title) {
		var queue = new ProgressQueue(title);
		var item = queue.addItem();
		queue.display();
		return item;
	}

	public static ProgressItem queueSingleItem(String title, ProgressItemNameFunction name) {
		var queue = new ProgressQueue(title);
		var item = queue.addItem("", name);
		queue.display();
		return item;
	}

	public static ProgressItem queueError(String title, String error) {
		var queue = new ProgressQueue(title);
		var item = queue.addItem();
		item.setSize(0L);

		if (!error.isEmpty()) {
			item.error(error);
		}
		queue.display();
		return item;
	}

	public final List<ProgressItem> items;
	public String topText;
	public String bottomText;
	public final List<ImText> errors;
	public boolean hideInGame;
	public boolean canCancel;
	public boolean open;
	public boolean active;

	public ProgressQueue(String topText) {
		this.items = new ArrayList<>(1);
		this.topText = topText;
		this.bottomText = "";
		this.errors = new ArrayList<>(0);
		this.hideInGame = false;
		this.canCancel = false;
		this.open = true;
		this.active = false;
	}

	public ProgressQueue() {
		this("Loading...");
	}

	public ProgressItem addItem(String label, ProgressItemNameFunction nameFunction) {
		var item = new ProgressItem(this, label, nameFunction);

		synchronized (ACTIVE) {
			items.add(item);
		}

		return item;
	}

	public ProgressItem addItem(String name) {
		return addItem(name, ProgressItemNameFunction.PERCENT);
	}

	public ProgressItem addItem() {
		return addItem("", ProgressItemNameFunction.PERCENT);
	}

	public void clear() {
		synchronized (ACTIVE) {
			items.clear();
		}
	}

	public void display() {
		synchronized (ACTIVE) {
			if (!active) {
				active = true;
				open = true;
				ACTIVE.add(this);
			}
		}
	}

	public boolean isCancelled() {
		return !open;
	}

	public void error(ImText error) {
		synchronized (ACTIVE) {
			errors.add(error);
		}
	}

	public void error(String error) {
		error(ImText.of(error));
	}

	public void warning(String error) {
		error(ImText.warning(error));
	}
}
