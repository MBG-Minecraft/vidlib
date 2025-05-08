package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.kmath.DistanceComparator;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
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

public class VidLibParticleRenderTypes {
	public static final ParticleRenderType TRUE_TRANSLUCENT = new ParticleRenderType("vidlib:true_translucent", RenderType.translucentParticle(TextureAtlas.LOCATION_PARTICLES), true);
	public static final ParticleRenderType ADDITIVE = new ParticleRenderType("vidlib:additive", VidLibRenderTypes.Particle.ADDITIVE, true);

	public static final Map<ParticleRenderType, List<Particle>> TEMP_LIST = new IdentityHashMap<>();

	static {
		TEMP_LIST.put(TRUE_TRANSLUCENT, new ArrayList<>());
		TEMP_LIST.put(ADDITIVE, new ArrayList<>());
	}

	@Unique
	private static List<Particle> vl$sortedParticleList = null;

	public static void vl$renderParticleTypePre(ParticleRenderType particleType) {
		vl$sortedParticleList = VidLibParticleRenderTypes.TEMP_LIST.get(particleType);

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

			vl$sortedParticleList.clear();
			vl$sortedParticleList = null;
		}
	}
}
