package dev.latvian.mods.vidlib.feature.prop;

public record RenderedProp<P extends Prop>(P prop, PropRenderer<P> renderer) {
}
