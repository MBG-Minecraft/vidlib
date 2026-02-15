package dev.latvian.mods.vidlib.core;

import net.minecraft.resources.ResourceLocation;

public interface VLTicketOwner<T extends Comparable<? super T>> {
	default ResourceLocation vl$getId() {
		throw new NoMixinException(this);
	}

	default T vl$getOwner() {
		throw new NoMixinException(this);
	}
}
