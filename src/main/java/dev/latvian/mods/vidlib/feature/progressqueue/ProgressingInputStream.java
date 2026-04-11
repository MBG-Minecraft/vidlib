package dev.latvian.mods.vidlib.feature.progressqueue;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressingInputStream extends FilterInputStream {
	public static InputStream wrap(InputStream in, @Nullable ProgressItem progressItem) {
		return in == null || progressItem == null ? in : new ProgressingInputStream(in, progressItem);
	}

	public final ProgressItem progressItem;

	public ProgressingInputStream(InputStream in, ProgressItem progressItem) {
		super(in);
		this.progressItem = progressItem;
	}

	@Override
	public int read() throws IOException {
		if (progressItem.queue.isCancelled()) {
			throw new ProgressCancelledException(progressItem);
		}

		int result = super.read();

		if (result != -1) {
			progressItem.addProgress(1L);
		}

		return result;
	}

	@Override
	public int read(@NonNull byte[] b) throws IOException {
		if (progressItem.queue.isCancelled()) {
			throw new ProgressCancelledException(progressItem);
		}

		int result = super.read(b);
		progressItem.addProgress(result);
		return result;
	}

	@Override
	public int read(@NonNull byte[] b, int off, int len) throws IOException {
		if (progressItem.queue.isCancelled()) {
			throw new ProgressCancelledException(progressItem);
		}

		int result = super.read(b, off, len);
		progressItem.addProgress(result);
		return result;
	}

	@Override
	public long skip(long n) throws IOException {
		if (progressItem.queue.isCancelled()) {
			throw new ProgressCancelledException(progressItem);
		}

		long result = super.skip(n);
		progressItem.addProgress(result);
		return result;
	}
}
