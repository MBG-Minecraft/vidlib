package dev.latvian.mods.vidlib.core;

public class NoMixinException extends IllegalStateException {
	public NoMixinException(Object thisObject) {
		super("A mixin should have implemented this method! Missing in " + thisObject.getClass().getName());
	}
}
