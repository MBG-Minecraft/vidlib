package dev.beast.mods.shimmer.core;

public class NoMixinException extends IllegalStateException {
	public NoMixinException() {
		super("A mixin should have implemented this method!");
	}
}
