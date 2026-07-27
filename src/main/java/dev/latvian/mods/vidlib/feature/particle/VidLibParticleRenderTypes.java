package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.math.DistanceComparator;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderPipelines;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;

import java.util.ArrayList;

@EventBusSubscriber(modid = VidLib.ID, value = Dist.CLIENT)
public class VidLibParticleRenderTypes {
	public static final ParticleRenderType TRUE_TRANSLUCENT = new ParticleRenderType("vidlib:true_translucent");
	public static final ParticleRenderType ADDITIVE = new ParticleRenderType("vidlib:additive");
	public static final ParticleRenderType ADDITIVE_ONLY_DEPTH = new ParticleRenderType("vidlib:additive_only_depth");
	public static final ParticleRenderType CUSTOM = new ParticleRenderType("vidlib:custom");

	public static final SingleQuadParticle.Layer TRUE_TRANSLUCENT_LAYER = new SingleQuadParticle.Layer(true, TextureAtlas.LOCATION_PARTICLES, net.minecraft.client.renderer.RenderPipelines.TRANSLUCENT_PARTICLE);
	public static final SingleQuadParticle.Layer ADDITIVE_LAYER = new SingleQuadParticle.Layer(true, TextureAtlas.LOCATION_PARTICLES, VidLibRenderPipelines.ADDITIVE_PARTICLE);
	public static final SingleQuadParticle.Layer ADDITIVE_ONLY_DEPTH_LAYER = new SingleQuadParticle.Layer(true, TextureAtlas.LOCATION_PARTICLES, VidLibRenderPipelines.ADDITIVE_PARTICLE_ONLY_DEPTH);

	@SubscribeEvent
	public static void registerParticleGroups(RegisterParticleGroupsEvent event) {
		event.register(TRUE_TRANSLUCENT, SortedQuadParticleGroup::new);
		event.register(ADDITIVE, SortedQuadParticleGroup::new);
		event.register(ADDITIVE_ONLY_DEPTH, SortedQuadParticleGroup::new);
		event.register(CUSTOM, CustomParticleGroup::new);
	}

	public static void renderCustomParticles(com.mojang.blaze3d.vertex.PoseStack poseStack, MultiBufferSource buffers, Camera camera, float delta) {
		if (CustomParticleGroup.instance != null) {
			CustomParticleGroup.instance.render(poseStack, buffers, camera, delta);
		}
	}

	private static class SortedQuadParticleGroup extends ParticleGroup<SingleQuadParticle> {
		private final QuadParticleRenderState renderState;
		private final ArrayList<SingleQuadParticle> sortedParticles;

		private SortedQuadParticleGroup(ParticleEngine engine) {
			super(engine);
			this.renderState = new QuadParticleRenderState();
			this.sortedParticles = new ArrayList<>();
		}

		@Override
		public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
			renderState.clear();
			sortedParticles.clear();

			for (var particle : particles) {
				var pos = particle.getPos();

				if (frustum.pointInFrustum(pos.x, pos.y, pos.z)) {
					sortedParticles.add(particle);
				}
			}

			if (sortedParticles.size() >= 2) {
				sortedParticles.sort(new DistanceComparator<>(camera.position(), SingleQuadParticle::getPos));
			}

			for (var particle : sortedParticles) {
				particle.extract(renderState, camera, partialTickTime);
			}

			sortedParticles.clear();
			return renderState;
		}
	}

	private static class CustomParticleGroup extends ParticleGroup<CustomParticle> {
		private static CustomParticleGroup instance;
		private Frustum frustum;

		private CustomParticleGroup(ParticleEngine engine) {
			super(engine);
			instance = this;
		}

		@Override
		public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
			this.frustum = frustum;
			return (submitNodeCollector, cameraRenderState) -> {
			};
		}

		private void render(com.mojang.blaze3d.vertex.PoseStack poseStack, MultiBufferSource buffers, Camera camera, float delta) {
			for (var particle : particles) {
				if (particle.shouldRender(camera, frustum, delta)) {
					particle.renderCustom(poseStack, buffers, camera, delta);
				}
			}
		}
	}
}
