package dev.latvian.mods.vidlib.feature.progressqueue;

import dev.latvian.mods.vidlib.feature.imgui.ImText;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.ApiStatus;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ProgressQueue {
	@ApiStatus.Internal
	public static final ConcurrentLinkedDeque<ProgressQueue> ACTIVE = new ConcurrentLinkedDeque<>();

	@ApiStatus.Internal
	public static final AtomicInteger ACTIVE_COUNT = new AtomicInteger(0);

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
	public final Deque<ImText> errors;
	public boolean hideInGame;
	public boolean canCancel;
	public final ImBoolean open;
	public boolean active;

	public ProgressQueue(String topText) {
		this.items = new ConcurrentLinkedDeque<>();
		this.topText = topText;
		this.bottomText = "";
		this.errors = new ConcurrentLinkedDeque<>();
		this.hideInGame = false;
		this.canCancel = false;
		this.open = new ImBoolean(true);
		this.active = false;
	}

	public ProgressQueue() {
		this("Loading...");
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
		if (!active) {
			active = true;
			open.set(true);
			ACTIVE.add(this);
			ACTIVE_COUNT.incrementAndGet();
		}
	}

	public boolean isCancelled() {
		return !open.get();
	}

	public void error(ImText error) {
		errors.add(error);
	}

	public void error(String error) {
		error(ImText.of(error));
	}

	public void warning(String error) {
		error(ImText.warning(error));
	}
}
