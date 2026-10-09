package dev.latvian.mods.vidlib.feature.progressqueue;

import dev.latvian.mods.vidlib.util.ColoredText;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public final class ProgressItem implements IntConsumer, LongConsumer {
	public final ProgressQueue queue;
	public final AtomicInteger status;
	public final AtomicLong progress;
	public final AtomicLong size;
	public String label;
	public ProgressItemNameFunction infoText;
	public boolean blocksExit;

	public ProgressItem(ProgressQueue queue, String label, ProgressItemNameFunction infoText) {
		this.queue = queue;
		this.status = new AtomicInteger(0);
		this.progress = new AtomicLong(0L);
		this.size = new AtomicLong(1L);
		this.label = label;
		this.infoText = infoText;
		this.blocksExit = false;
	}

	public void display() {
		status.set(1);
		queue.display();
	}

	public void remove() {
		status.set(2);
	}

	public boolean isVisible() {
		return status.get() == 1;
	}

	public boolean isRemoved() {
		return status.get() >= 2;
	}

	public boolean isBlockingExit() {
		return blocksExit && !isRemoved();
	}

	public void setSize(long size) {
		this.size.set(size);
	}

	public void addSize(long size) {
		this.size.addAndGet(size);
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

	public void error(ColoredText error) {
		queue.error(error);
		remove();
	}

	public void error(String error) {
		error(ColoredText.of(error));
	}

	public void error(Throwable error) {
		error(error.toString());
	}

	public void warning(String error) {
		error(ColoredText.warning(error));
	}

	@Override
	public void accept(int value) {
		addProgress(value);
	}

	@Override
	public void accept(long value) {
		addProgress(value);
	}

	public void setInfoText(ProgressItemNameFunction function) {
		infoText = function;
	}

	public void setInfoText(String string) {
		infoText = new ProgressItemNameFunction.OfString(string);
	}

	public void parseInfoText(String string) {
		switch (string) {
			case "%" -> setInfoText(ProgressItemNameFunction.PERCENT);
			case "N" -> setInfoText(ProgressItemNameFunction.COUNT);
			case "iB" -> setInfoText(ProgressItemNameFunction.BINARY_BYTE_SIZE);
			case "B" -> setInfoText(ProgressItemNameFunction.SI_BYTE_SIZE);
			default -> setInfoText(string);
		}
	}

	public void setBlocksExit(boolean blocksExit) {
		this.blocksExit = blocksExit;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}