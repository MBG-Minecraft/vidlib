package dev.latvian.mods.vidlib.feature.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

import java.util.LinkedList;
import java.util.List;

public class CustomParticleGroup extends ParticleGroup<CustomParticle> {
	private final CustomParticleRenderState state;

	public CustomParticleGroup(ParticleEngine engine) {
		super(engine);
		this.state = new CustomParticleRenderState();
	}

	@Override
	public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
		var cameraPos = camera.position();

		for (var particle : particles) {
			var s = particle.extract(frustum, camera, cameraPos, partialTickTime);

			if (s != null) {
				state.particles.add(s);
			}
		}

		return state;
	}

	public static class CustomParticleRenderState implements ParticleGroupRenderState {
		private final List<CustomParticleState> particles = new LinkedList<>();

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
