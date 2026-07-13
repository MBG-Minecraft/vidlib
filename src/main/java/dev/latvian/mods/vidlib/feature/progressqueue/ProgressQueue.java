package dev.latvian.mods.vidlib.feature.progressqueue;

import dev.latvian.mods.vidlib.util.ColoredText;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ProgressQueue {
	public static final ReentrantLock LOCK = new ReentrantLock();

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

	public static boolean isBlockingExit() {
		LOCK.lock();

		try {
			for (var queue : ProgressQueue.ACTIVE) {
				for (var item : queue.items) {
					if (item.isBlockingExit()) {
						return true;
					}
				}
			}

			return false;
		} finally {
			LOCK.unlock();
		}
	}

	public final List<ProgressItem> items;
	public String topText;
	public String bottomText;
	public final List<ColoredText> errors;
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

		LOCK.lock();

		try {
			items.add(item);
		} finally {
			LOCK.unlock();
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
		LOCK.lock();

		try {
			items.clear();
		} finally {
			LOCK.unlock();
		}
	}

	public void display() {
		LOCK.lock();

		try {
			if (!active) {
				active = true;
				open = true;
				ACTIVE.add(this);
			}
		} finally {
			LOCK.unlock();
		}
	}

	public boolean isCancelled() {
		return !open;
	}

	public void error(ColoredText error) {
		LOCK.lock();

		try {
			errors.add(error);
		} finally {
			LOCK.unlock();
		}
	}

	public void error(String error) {
		error(ColoredText.of(error));
	}

	public void warning(String error) {
		error(ColoredText.warning(error));
	}
}
