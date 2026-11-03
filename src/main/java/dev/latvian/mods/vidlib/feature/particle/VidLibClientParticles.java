package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.vidlib.feature.client.VidLibRenderPipelines;
import dev.latvian.mods.vidlib.feature.actor.ActorParticle;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesRenderTypes;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public interface VidLibClientParticles {
	ParticleRenderType ADDITIVE_RENDER_TYPE = new ParticleRenderType("VIDLIB_ADDITIVE", "VL_A");
	ParticleRenderType ADDITIVE_ONLY_DEPTH_RENDER_TYPE = new ParticleRenderType("VIDLIB_ADDITIVE_ONLY_DEPTH", "VL_AOD");
	ParticleRenderType CUSTOM_RENDER_TYPE = new ParticleRenderType("VIDLIB_CUSTOM", "VL_C");

	SingleQuadParticle.Layer TRUE_TRANSLUCENT_LAYER = new SingleQuadParticle.Layer(
		true,
		TextureAtlas.LOCATION_PARTICLES,
		RenderPipelines.TRANSLUCENT_PARTICLE,
		RenderPipelines.OIT_PARTICLE
	);

	SingleQuadParticle.Layer ADDITIVE_LAYER = new SingleQuadParticle.Layer(
		true,
		TextureAtlas.LOCATION_PARTICLES,
		VidLibRenderPipelines.ADDITIVE_PARTICLE,
		RenderPipelines.OIT_PARTICLE
	);

	SingleQuadParticle.Layer ADDITIVE_ONLY_DEPTH_LAYER = new SingleQuadParticle.Layer(
		true,
		TextureAtlas.LOCATION_PARTICLES,
		VidLibRenderPipelines.ADDITIVE_PARTICLE_ONLY_DEPTH,
		RenderPipelines.OIT_PARTICLE
	);

	interface Physics {
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

	static void registerParticleProviders(RegisterParticleProvidersEvent event) {
		event.registerSpecial(VidLibParticles.SHAPE.get(), ShapeParticle::new);
		// event.registerSpecial(VidLibParticles.LINE.get(), LineParticle::new);
		// event.registerSpecial(VidLibParticles.TEXT.get(), TextParticle::new);
		// event.registerSpecial(VidLibParticles.ITEM.get(), ItemParticle::new);
		event.registerSpecial(VidLibParticles.ACTOR.get(), ActorParticle::create);
		// event.registerSpecial(VidLibParticles.LIGHTNING.get(), LightningParticle::new);
		// event.registerSpriteSet(VidLibParticles.BURN_SMOKE.get(), BurnSmokeParticle::create);
		// event.registerSpriteSet(VidLibParticles.SPARK.get(), SparkParticle::create);
		// event.registerSpriteSet(VidLibParticles.WIND.get(), WindParticle::create);
		// event.registerSpriteSet(VidLibParticles.FIRE.get(), FireParticle::create);
	}

	static void registerParticleGroups(RegisterParticleGroupsEvent event) {
		event.register(CUSTOM_RENDER_TYPE, CustomParticleGroup::new);
	}
}
