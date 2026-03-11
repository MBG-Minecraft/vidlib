package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayAPI;
import it.unimi.dsi.fastutil.chars.CharConsumer;

public class ReplayIconsEvent extends ReplayEvent {
	private final CharConsumer callback;

	public ReplayIconsEvent(ReplayAPI api, CharConsumer callback) {
		super(api);
		this.callback = callback;
	}

	public void add(char icon) {
		callback.accept(icon);
	}

	public void add(String icons) {
		callback.accept(0);

		for (var c : icons.toCharArray()) {
			callback.accept(c);
		}
	}
}
