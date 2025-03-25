package dev.beast.mods.shimmer.core.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.beast.mods.shimmer.feature.particle.ShimmerParticleRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TrackingEmitter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
	@Shadow
	@Final
	private Map<ParticleRenderType, Queue<Particle>> particles;

	@Shadow
	protected ClientLevel level;

	@Shadow
	protected abstract void tickParticleList(Collection<Particle> particles);

	@Shadow
	@Final
	private Queue<TrackingEmitter> trackingEmitters;

	@Shadow
	@Final
	private Queue<Particle> particlesToAdd;

	/**
	 * @author Lat
	 * @reason Optimization and remove particle limit
	 */
	@Overwrite
	public void tick() {
		for (var entry : particles.entrySet()) {
			tickParticleList(entry.getValue());
		}

		if (!trackingEmitters.isEmpty()) {
			var itr = trackingEmitters.iterator();

			while (itr.hasNext()) {
				var emitter = itr.next();
				emitter.tick();

				if (!emitter.isAlive()) {
					itr.remove();
				}
			}
		}

		Particle particle;
		if (!particlesToAdd.isEmpty()) {
			while ((particle = particlesToAdd.poll()) != null) {
				var type = particle.getRenderType();
				var queue = particles.get(type);

				if (queue == null) {
					queue = new ArrayDeque<>(256);
					particles.put(type, queue);
				}

				queue.add(particle);
			}
		}
	}

	@Inject(method = "renderParticleType(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/particle/ParticleRenderType;Ljava/util/Queue;Lnet/minecraft/client/renderer/culling/Frustum;)V", at = @At("HEAD"))
	private static void shimmer$renderParticleTypePre(Camera camera, float partialTick, MultiBufferSource.BufferSource bufferSource, ParticleRenderType particleType, Queue<Particle> particles, Frustum frustum, CallbackInfo ci) {
		ShimmerParticleRenderTypes.shimmer$renderParticleTypePre(particleType);
	}

	@Redirect(method = "renderParticleType(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/particle/ParticleRenderType;Ljava/util/Queue;Lnet/minecraft/client/renderer/culling/Frustum;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V"))
	private static void shimmer$renderParticle(Particle instance, VertexConsumer vertexConsumer, Camera camera, float v) {
		if (ShimmerParticleRenderTypes.addDefaultParticle(instance)) {
			instance.render(vertexConsumer, camera, v);
		}
	}

	@Inject(method = "renderParticleType(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/particle/ParticleRenderType;Ljava/util/Queue;Lnet/minecraft/client/renderer/culling/Frustum;)V", at = @At("RETURN"))
	private static void shimmer$renderParticleTypePost(Camera camera, float partialTick, MultiBufferSource.BufferSource bufferSource, ParticleRenderType particleType, Queue<Particle> particles, Frustum frustum, CallbackInfo ci) {
		ShimmerParticleRenderTypes.shimmer$renderParticleTypePost(camera, partialTick, bufferSource, particleType);
	}
}
