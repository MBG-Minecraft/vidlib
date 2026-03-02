package dev.latvian.mods.vidlib.feature.progressqueue;

import java.io.IOException;

public class ProgressCancelledException extends IOException {
	public final ProgressItem item;

	public ProgressCancelledException(ProgressItem item) {
		super("Progress cancelled");
		this.item = item;
	}
}
