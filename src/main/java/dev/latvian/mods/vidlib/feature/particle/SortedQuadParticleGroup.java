package dev.latvian.mods.vidlib.feature.particle;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;

import java.util.ArrayList;
import java.util.List;

/**
 * 26.3 replacement for VidLib's render-type-driven particle sorting.
 * <p>
 * In 1.21.5 {@code VidLibParticleRenderTypes} hooked {@code ParticleEngine.renderParticleType} to
 * collect every particle of a {@code ParticleRenderType}, sort it far-to-near and then emit it. In
 * 26.3 the engine no longer renders anything itself - it only
 * {@link ParticleEngine#extract(net.minecraft.client.renderer.state.level.ParticlesRenderState, Frustum, Camera, float)}
 * per-frame state - so the sorting happens here, at extraction time.
 */
public class SortedQuadParticleGroup extends ParticleGroup<SingleQuadParticle> {
	private final ParticleRenderType particleType;
	private final VidLibQuadParticleRenderState state = new VidLibQuadParticleRenderState();

	public SortedQuadParticleGroup(ParticleEngine engine, ParticleRenderType particleType) {
		super(engine);
		this.particleType = particleType;
	}

	@Override
	public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
		state.clear();

		List<SingleQuadParticle> visible = new ArrayList<>();

		for (SingleQuadParticle particle : this.particles) {
			if (frustum.isVisible(particle.getBoundingBox())) {
				visible.add(particle);
			}
		}

		if (VidLibParticleRenderTypes.isSorted(particleType) && visible.size() >= 2) {
			visible.sort(VidLibParticleRenderTypes.distanceComparator(camera));
		}

		boolean depthPass = VidLibParticleRenderTypes.hasDepthPass(particleType);

		for (SingleQuadParticle particle : visible) {
			try {
				if (depthPass && particle instanceof DepthPassQuadParticle depthParticle) {
					depthParticle.vl$setForcedLayer(VidLibParticleRenderTypes.ADDITIVE_ONLY_DEPTH_LAYER);
					particle.extract(state.depth, camera, partialTickTime);
					depthParticle.vl$setForcedLayer(null);
				}

				particle.extract(state.main, camera, partialTickTime);
			} catch (Throwable throwable) {
				CrashReport report = CrashReport.forThrowable(throwable, "Rendering Particle");
				CrashReportCategory crashreportcategory = report.addCategory("Particle being rendered");
				crashreportcategory.setDetail("Particle", particle::toString);
				crashreportcategory.setDetail("Particle Type", this.particleType::toString);
				throw new ReportedException(report);
			}
		}

		return state;
	}

	/**
	 * Holds the extracted quads of one frame. The depth pass is empty for every render
	 * type except {@link VidLibParticleRenderTypes#ADDITIVE}.
	 */
	public static class VidLibQuadParticleRenderState implements ParticleGroupRenderState {
		public final QuadParticleRenderState main = new QuadParticleRenderState();
		public final QuadParticleRenderState depth = new QuadParticleRenderState();

		@Override
		public void clear() {
			main.clear();
			depth.clear();
		}

		@Override
		public void submit(SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
			if (!main.isEmpty()) {
				submitNodeCollector.submitQuadParticleGroup(main);
			}

			if (!depth.isEmpty()) {
				submitNodeCollector.submitQuadParticleGroup(depth);
			}
		}
	}
}
