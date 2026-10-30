package dev.latvian.mods.vidlib.feature.particle;

import net.minecraft.client.particle.SingleQuadParticle;

/**
 * Opt-in hook for quad particles that need to be rendered into a second
 * {@link SingleQuadParticle.Layer} on top of their regular one.
 * <p>
 * 1.21.5 did this by re-rendering the whole sorted particle list into a second
 * {@code RenderType}; in 26.3 the layer is chosen per quad, so the group temporarily
 * overrides the particle's own layer.
 */
public interface DepthPassQuadParticle {
	void vl$setForcedLayer(SingleQuadParticle.Layer layer);
}
