package dev.latvian.mods.vidlib.feature.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

import java.util.LinkedList;
import java.util.List;

public class CustomParticleGroup extends ParticleGroup<CustomParticle> {
	private final ParticleRenderType particleType;
	private final CustomParticleRenderState state = new CustomParticleRenderState();

	public CustomParticleGroup(ParticleEngine engine, ParticleRenderType particleType) {
		super(engine);
		this.particleType = particleType;
	}

	@Override
	public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
		state.clear();

		for (var particle : this.particles) {
			if (particle.extract(frustum, camera, partialTickTime)) {
				state.add(particle);
			}
		}

		return state;
	}

	public static class CustomParticleRenderState implements ParticleGroupRenderState {
		private final List<CustomParticle> particles = new LinkedList<>();

		public void add(CustomParticle particle) {
			particles.add(particle);
		}

		@Override
		public void clear() {
			particles.clear();
		}

		@Override
		public void submit(SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
			for (var particle : particles) {
				particle.submit(collector, cameraRenderState);
			}
		}
	}
}
