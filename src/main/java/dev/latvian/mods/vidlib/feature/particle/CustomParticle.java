package dev.latvian.mods.vidlib.feature.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * Base class for particles that draw with custom geometry rather than a billboarded sprite quad.
 * <p>
 * 1.21.5 rendered these through {@code ParticleEngine.renderParticleType} with a
 * {@code MultiBufferSource}; 26.3 removed that path entirely, so they are now extracted into a
 * {@link CustomParticleGroup} render state and drawn through
 * {@link SubmitNodeCollector#submitCustomGeometry}.
 */
public abstract class CustomParticle extends Particle {
	public int prevAge;

	public CustomParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z, vx, vy, vz);
		setSize(1F, 1F);
	}

	public CustomParticle(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
		setSize(1F, 1F);
	}

	@Override
	public void tick() {
		prevAge = age;
		super.tick();
	}

	@Override
	public ParticleRenderType getGroup() {
		return VidLibParticleRenderTypes.CUSTOM;
	}

	public abstract boolean extract(Frustum frustum, Camera camera, float delta);

	public abstract void submit(SubmitNodeCollector collector, CameraRenderState cameraRenderState);
}
