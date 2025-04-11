package dev.beast.mods.shimmer.feature.particle;

import dev.latvian.mods.kmath.DistanceComparator;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShimmerParticleRenderTypes {
	public static final ParticleRenderType TRANSLUCENT_ADDITION = new ParticleRenderType("shimmer:translucent_addition", RenderType.translucentParticle(TextureAtlas.LOCATION_PARTICLES), true);
	public static final ParticleRenderType TRUE_TRANSLUCENT = new ParticleRenderType("shimmer:true_translucent", RenderType.translucentParticle(TextureAtlas.LOCATION_PARTICLES), true);

	public static final Map<ParticleRenderType, List<Particle>> TEMP_LIST = new IdentityHashMap<>();

	static {
		TEMP_LIST.put(TRUE_TRANSLUCENT, new ArrayList<>());
	}

	@Unique
	private static List<Particle> shimmer$sortedParticleList = null;

	public static void shimmer$renderParticleTypePre(ParticleRenderType particleType) {
		shimmer$sortedParticleList = ShimmerParticleRenderTypes.TEMP_LIST.get(particleType);

		if (shimmer$sortedParticleList != null) {
			shimmer$sortedParticleList.clear();
		}
	}

	public static boolean addDefaultParticle(Particle particle) {
		if (shimmer$sortedParticleList != null) {
			shimmer$sortedParticleList.add(particle);
			return false;
		}

		return true;
	}

	public static void shimmer$renderParticleTypePost(Camera camera, float partialTick, MultiBufferSource.BufferSource bufferSource, ParticleRenderType particleType) {
		if (shimmer$sortedParticleList != null && !shimmer$sortedParticleList.isEmpty()) {
			if (shimmer$sortedParticleList.size() >= 2) {
				shimmer$sortedParticleList.sort(new DistanceComparator<>(camera.getPosition(), Particle::getPos));
			}

			var buffer = bufferSource.getBuffer(Objects.requireNonNull(particleType.renderType()));

			for (var particle : shimmer$sortedParticleList) {
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

			shimmer$sortedParticleList.clear();
			shimmer$sortedParticleList = null;
		}
	}
}
