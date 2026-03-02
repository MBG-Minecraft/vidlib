package dev.latvian.mods.vidlib.feature.progressqueue;

import imgui.type.ImBoolean;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ProgressQueue {
	public static final ConcurrentLinkedDeque<ProgressQueue> ACTIVE = new ConcurrentLinkedDeque<>();

	public static ProgressItem queueSingleItem(String title) {
		var queue = new ProgressQueue();
		queue.topText = title;
		var item = queue.addItem();
		queue.display();
		return item;
	}

	public static ProgressItem queueSingleItem(String title, ProgressItemNameFunction name) {
		var queue = new ProgressQueue();
		queue.topText = title;
		var item = queue.addItem(name);
		queue.display();
		return item;
	}

	public static ProgressItem queueError(String title, String error) {
		var queue = new ProgressQueue();
		queue.topText = title;
		var item = queue.addItem();
		item.setSize(0L);

		if (!error.isEmpty()) {
			item.error(error);
		}
		queue.display();
		return item;
	}

	public final Deque<ProgressItem> items;
	public String topText;
	public String bottomText;
	public final Deque<String> errors;
	public boolean hideInGame;
	public boolean canCancel;
	public final ImBoolean open;

	public ProgressQueue() {
		this.items = new ConcurrentLinkedDeque<>();
		this.topText = "Loading...";
		this.bottomText = "";
		this.errors = new ConcurrentLinkedDeque<>();
		this.hideInGame = false;
		this.canCancel = false;
		this.open = new ImBoolean(true);
	}

	public ProgressItem addItem(ProgressItemNameFunction nameFunction) {
		var item = new ProgressItem(this, new AtomicInteger(0), new AtomicLong(0L), new AtomicLong(1L), nameFunction);
		items.add(item);
		return item;
	}

	public ProgressItem addItem(String name) {
		return addItem(new ProgressItemNameFunction.OfString(name));
	}

	public ProgressItem addItem() {
		return addItem(ProgressItemNameFunction.PERCENT);
	}

	public void display() {
		ACTIVE.add(this);
	}

	public boolean isCancelled() {
		return canCancel && !open.get();
	}
}
