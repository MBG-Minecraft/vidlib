package dev.latvian.mods.vidlib.feature.decal;

import net.neoforged.bus.api.Event;

import java.util.List;

public class DecalEvent extends Event {
	private final List<Decal> list;

	public DecalEvent(List<Decal> list) {
		this.list = list;
	}

	public void add(Decal decal) {
		decal.addToList(list);
	}
}
