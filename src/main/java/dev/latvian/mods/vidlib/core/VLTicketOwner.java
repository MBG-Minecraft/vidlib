package dev.latvian.mods.vidlib.core;

import net.minecraft.resources.Identifier;

public interface VLTicketOwner<T extends Comparable<? super T>> {
	default Identifier vl$getId() {
		throw new NoMixinException(this);
	}

	default T vl$getOwner() {
		throw new NoMixinException(this);
	}
}
