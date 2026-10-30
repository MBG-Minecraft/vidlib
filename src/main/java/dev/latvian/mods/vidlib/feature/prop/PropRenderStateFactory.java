package dev.latvian.mods.vidlib.feature.prop;

@FunctionalInterface
public interface PropRenderStateFactory<P extends Prop, S extends PropRenderState<P>> {
	S create();
}
