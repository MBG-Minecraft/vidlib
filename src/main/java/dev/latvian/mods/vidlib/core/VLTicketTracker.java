package dev.latvian.mods.vidlib.core;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.util.Set;

public interface VLTicketTracker<T extends Comparable<? super T>> {
	default Long2ObjectMap<Set<VLTicketOwner<T>>> vl$getTickets() {
		throw new NoMixinException(this);
	}
}
