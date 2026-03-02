package dev.latvian.mods.vidlib.feature.progressqueue;

import org.jetbrains.annotations.NotNull;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ProgressingOutputStream extends FilterOutputStream {
	public final ProgressItem progressItem;

	public ProgressingOutputStream(OutputStream out, ProgressItem progressItem) {
		super(out);
		this.progressItem = progressItem;
	}

	@Override
	public void write(int b) throws IOException {
		if (progressItem.queue().isCancelled()) {
			throw new ProgressCancelledException(progressItem);
		}

		super.write(b);
		progressItem.addProgress(1L);
	}

	@Override
	public void write(@NotNull byte[] b) throws IOException {
		if (progressItem.queue().isCancelled()) {
			throw new ProgressCancelledException(progressItem);
		}

		super.write(b);
		progressItem.addProgress(b.length);
	}

	@Override
	public void write(@NotNull byte[] b, int off, int len) throws IOException {
		if (progressItem.queue().isCancelled()) {
			throw new ProgressCancelledException(progressItem);
		}

		super.write(b, off, len);
		progressItem.addProgress(len);
	}
}
