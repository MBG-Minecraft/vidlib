package dev.latvian.mods.vidlib.feature.progressqueue;

import dev.latvian.mods.vidlib.feature.imgui.ImText;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class ProgressItem {
	public final ProgressQueue queue;
	public final AtomicInteger status;
	public final AtomicLong progress;
	public final AtomicLong size;
	public String label;
	public ProgressItemNameFunction nameFunction;

	public ProgressItem(ProgressQueue queue, String label, ProgressItemNameFunction nameFunction) {
		this.queue = queue;
		this.status = new AtomicInteger(0);
		this.progress = new AtomicLong(0L);
		this.size = new AtomicLong(1L);
		this.label = label;
		this.nameFunction = nameFunction;
	}

	public void setStarted() {
		status.set(1);
	}

	public void setDone() {
		status.set(2);
	}

	public boolean isVisible() {
		return status.get() == 1;
	}

	public boolean isDone() {
		return status.get() == 2;
	}

	public void setSize(long size) {
		this.size.set(size);
	}

	public long resetProgress() {
		return this.progress.getAndSet(0L);
	}

	public void setProgress(long progress) {
		this.progress.set(progress);
	}

	public long addProgress(long progress) {
		return this.progress.addAndGet(progress);
	}

	public void error(ImText error) {
		queue.error(error);
		setDone();
	}

	public void error(String error) {
		error(ImText.of(error));
	}

	public void warning(String error) {
		error(ImText.warning(error));
	}
}