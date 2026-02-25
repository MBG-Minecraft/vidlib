package dev.latvian.mods.vidlib.feature.progressqueue;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressingInputStream extends FilterInputStream {
	public final ProgressItem progressItem;

	public ProgressingInputStream(InputStream in, ProgressItem progressItem) {
		super(in);
		this.progressItem = progressItem;
	}

	@Override
	public int read() throws IOException {
		int result = super.read();

		if (result != -1) {
			progressItem.addProgress(1L);
		}

		return result;
	}

	@Override
	public int read(@NonNull byte[] b) throws IOException {
		int result = super.read(b);
		progressItem.addProgress(result);
		return result;
	}

	@Override
	public int read(@NonNull byte[] b, int off, int len) throws IOException {
		int result = super.read(b, off, len);
		progressItem.addProgress(result);
		return result;
	}

	@Override
	public long skip(long n) throws IOException {
		long result = super.skip(n);
		progressItem.addProgress(result);
		return result;
	}
}
