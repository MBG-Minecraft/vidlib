package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.kmath.DistanceComparator;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderPipelines;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.TriState;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VidLibParticleRenderTypes {
	public static final ParticleRenderType TRUE_TRANSLUCENT = new ParticleRenderType("vidlib:true_translucent", RenderType.translucentParticle(TextureAtlas.LOCATION_PARTICLES), true);

	public static final ParticleRenderType ADDITIVE = new ParticleRenderType("vidlib:additive", RenderType.create(
		VidLib.id("particle/additive").toString(),
		1536,
		false,
		false,
		VidLibRenderPipelines.ADDITIVE_PARTICLE,
		RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_PARTICLES, TriState.FALSE, false))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setOutputState(RenderStateShard.PARTICLES_TARGET)
			.createCompositeState(false)
	), true);

	public static final ParticleRenderType ADDITIVE_ONLY_DEPTH = new ParticleRenderType("vidlib:additive_only_depth", RenderType.create(
		VidLib.id("particle/additive_only_depth").toString(),
		1536,
		false,
		false,
		VidLibRenderPipelines.ADDITIVE_PARTICLE_ONLY_DEPTH,
		RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_PARTICLES, TriState.FALSE, false))
			.setLightmapState(RenderStateShard.LIGHTMAP)
			.setOutputState(RenderStateShard.PARTICLES_TARGET)
			.createCompositeState(false)
	), true);

	private static final Map<ParticleRenderType, List<Particle>> SORTED = new IdentityHashMap<>();

	public static void enableSorting(ParticleRenderType type) {
		SORTED.put(type, new ArrayList<>());
	}

	static {
		enableSorting(TRUE_TRANSLUCENT);
		enableSorting(ADDITIVE);
		enableSorting(ADDITIVE_ONLY_DEPTH);
	}

	@Unique
	private static List<Particle> vl$sortedParticleList = null;

	public static void vl$renderParticleTypePre(ParticleRenderType particleType) {
		vl$sortedParticleList = VidLibParticleRenderTypes.SORTED.get(particleType);

		if (vl$sortedParticleList != null) {
			vl$sortedParticleList.clear();
		}
	}

	public static boolean addDefaultParticle(Particle particle) {
		if (vl$sortedParticleList != null) {
			vl$sortedParticleList.add(particle);
			return false;
		}

		return true;
	}

	public static void vl$renderParticleTypePost(Camera camera, float partialTick, MultiBufferSource.BufferSource bufferSource, ParticleRenderType particleType) {
		if (vl$sortedParticleList != null && !vl$sortedParticleList.isEmpty()) {
			if (vl$sortedParticleList.size() >= 2) {
				vl$sortedParticleList.sort(new DistanceComparator<>(camera.getPosition(), Particle::getPos));
			}

			var buffer = bufferSource.getBuffer(Objects.requireNonNull(particleType.renderType()));

			for (var particle : vl$sortedParticleList) {
				try {
					particle.render(buffer, camera, partialTick);
				} catch (Throwable throwable) {
					var crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
					CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
					crashreportcategory.setDetail("Particle", particle::toString);
					crashreportcategory.setDetail("Particle Type", particleType::toString);
					throw new ReportedException(crashreport);
				}
			}

			if (particleType.renderType() == ADDITIVE.renderType()) {
				var buffer2 = bufferSource.getBuffer(Objects.requireNonNull(ADDITIVE_ONLY_DEPTH.renderType()));

				for (var particle : vl$sortedParticleList) {
					try {
						particle.render(buffer2, camera, partialTick);
					} catch (Throwable throwable) {
						var crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
						CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
						crashreportcategory.setDetail("Particle", particle::toString);
						crashreportcategory.setDetail("Particle Type", particleType::toString);
						throw new ReportedException(crashreport);
					}
				}
			}

			vl$sortedParticleList.clear();
			vl$sortedParticleList = null;
		}
	}
}
