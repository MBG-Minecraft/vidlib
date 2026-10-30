package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * 26.3 port of VidLib's custom particle render types.
 * <p>
 * Minecraft 26.3 replaced the old {@code ParticleRenderType(String, RenderType, boolean)} constructor
 * with a simple {@code (name, shorthand)} record, and moved the actual pipeline/render-type selection
 * into {@link SingleQuadParticle.Layer}. So the group tags (used to sort particles into
 * {@link net.minecraft.client.particle.ParticleGroup}s) and the layers (used to batch them into draws)
 * are now separate things.
 */
public class VidLibParticleRenderTypes {
	public static final ParticleRenderType TRUE_TRANSLUCENT = new ParticleRenderType("vidlib:true_translucent", "TT");
	public static final ParticleRenderType ADDITIVE = new ParticleRenderType("vidlib:additive", "AD");
	public static final ParticleRenderType ADDITIVE_ONLY_DEPTH = new ParticleRenderType("vidlib:additive_only_depth", "AOD");
	public static final ParticleRenderType CUSTOM = new ParticleRenderType("vidlib:custom", "VC");

	private static final List<ParticleRenderType> EXTRA_TYPES = List.of(TRUE_TRANSLUCENT, ADDITIVE, ADDITIVE_ONLY_DEPTH, CUSTOM);

	private static final List<ParticleRenderType> EXTENDED_RENDER_ORDER = java.util.stream.Stream.concat(
		List.of(ParticleRenderType.SINGLE_QUADS, ParticleRenderType.ITEM_PICKUP, ParticleRenderType.ELDER_GUARDIANS).stream(),
		EXTRA_TYPES.stream()
	).toList();

	/**
	 * The render types that are not part of vanilla's {@code ParticleEngine.RENDER_ORDER}.
	 */
	public static List<ParticleRenderType> all() {
		return EXTRA_TYPES;
	}

	/**
	 * Vanilla's render order plus VidLib's, for {@code ParticleEngine.extract}.
	 */
	public static List<ParticleRenderType> renderOrder() {
		return EXTENDED_RENDER_ORDER;
	}

	/**
	 * True when the given render type is handled by vanilla's own {@code ParticleGroup}s.
	 */
	public static boolean isVanillaType(ParticleRenderType type) {
		return type != TRUE_TRANSLUCENT && type != ADDITIVE && type != ADDITIVE_ONLY_DEPTH && type != CUSTOM;
	}

	// --- Render types / layers ---------------------------------------------------------------

	static RenderPipeline additivePipeline(String path, boolean depthWrite) {
		return RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
			.withLocation(ID.vidlib("pipeline/" + path))
			.withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
			.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, depthWrite))
			.build();
	}

	public static final SingleQuadParticle.Layer TRUE_TRANSLUCENT_LAYER = new SingleQuadParticle.Layer(
		true,
		TextureAtlas.LOCATION_PARTICLES,
		RenderPipelines.TRANSLUCENT_PARTICLE,
		RenderPipelines.OIT_PARTICLE
	);

	public static final SingleQuadParticle.Layer ADDITIVE_LAYER = new SingleQuadParticle.Layer(
		true,
		TextureAtlas.LOCATION_PARTICLES,
		additivePipeline("particle/additive", true),
		RenderPipelines.OIT_PARTICLE
	);

	public static final SingleQuadParticle.Layer ADDITIVE_ONLY_DEPTH_LAYER = new SingleQuadParticle.Layer(
		true,
		TextureAtlas.LOCATION_PARTICLES,
		additivePipeline("particle/additive_only_depth", true),
		RenderPipelines.OIT_PARTICLE
	);

	// --- Sorting -----------------------------------------------------------------------------

	private static final Map<ParticleRenderType, Boolean> SORTED = new IdentityHashMap<>();

	public static void enableSorting(ParticleRenderType type) {
		SORTED.put(type, Boolean.TRUE);
	}

	public static boolean isSorted(ParticleRenderType type) {
		return type != null && SORTED.containsKey(type);
	}

	/**
	 * ADDITIVE particles are drawn twice - once with the additive layer, once with the
	 * additive-only-depth layer (this mirrors 1.21.5, where the whole sorted particle list was
	 * re-rendered into a second render pass).
	 */
	public static boolean hasDepthPass(ParticleRenderType type) {
		return type == ADDITIVE;
	}

	static {
		enableSorting(TRUE_TRANSLUCENT);
		enableSorting(ADDITIVE);
		enableSorting(ADDITIVE_ONLY_DEPTH);
	}

	/**
	 * Far-to-near ordering, matching {@code DistanceComparator}'s 1.21.5 behaviour.
	 * <p>
	 * {@code Particle}'s position fields are protected, so the bounding box centre - which differs
	 * only by the constant half-height - is used instead.
	 */
	public static Comparator<SingleQuadParticle> distanceComparator(Camera camera) {
		Vec3 origin = camera.position();
		return (a, b) -> Double.compare(
			origin.distanceToSqr(b.getBoundingBox().getCenter()),
			origin.distanceToSqr(a.getBoundingBox().getCenter())
		);
	}

	/**
	 * Render types used by {@link PhysicsParticleManager} when Iris is not running.
	 */
	public interface Physics {
		RenderType SOLID = RenderType.create("physics_particle_solid", RenderSetup.builder(PhysicsParticlesRenderTypes.SOLID_PIPELINE)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.useLightmap()
			.createRenderSetup());

		RenderType CUTOUT = RenderType.create("physics_particle_cutout", RenderSetup.builder(PhysicsParticlesRenderTypes.CUTOUT_PIPELINE)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.useLightmap()
			.createRenderSetup());

		RenderType CUTOUT_MIPPED = RenderType.create("physics_particle_cutout_mipped", RenderSetup.builder(PhysicsParticlesRenderTypes.CUTOUT_MIPPED_PIPELINE)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.useLightmap()
			.createRenderSetup());

		RenderType TRANSLUCENT = RenderType.create("physics_particle_translucent", RenderSetup.builder(PhysicsParticlesRenderTypes.TRANSLUCENT_PIPELINE)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.useLightmap()
			.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.createRenderSetup());

		RenderType TRIPWIRE = RenderType.create("physics_particle_tripwire", RenderSetup.builder(PhysicsParticlesRenderTypes.SOLID_PIPELINE)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.useLightmap()
			.createRenderSetup());
	}
}
