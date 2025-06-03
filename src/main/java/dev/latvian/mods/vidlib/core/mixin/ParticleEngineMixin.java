package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.vidlib.core.VLParticleEngine;
import dev.latvian.mods.vidlib.feature.particle.VidLibParticleRenderTypes;
import dev.latvian.mods.vidlib.util.Cast;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin implements VLParticleEngine {
	@Shadow
	protected ClientLevel level;

	@Shadow
	@Final
	private Map<ResourceLocation, ParticleEngine.MutableSpriteSet> spriteSets;

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"))
	private <K, V> V vl$createQueue(Map<K, V> instance, K key, Function<? super K, ? extends V> factory) {
		return instance.computeIfAbsent(key, k -> Cast.to(new ArrayDeque<>(16384)));
	}

	@Inject(method = "renderParticleType(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/particle/ParticleRenderType;Ljava/util/Queue;Lnet/minecraft/client/renderer/culling/Frustum;)V", at = @At("HEAD"))
	private static void vl$renderParticleTypePre(Camera camera, float partialTick, MultiBufferSource.BufferSource bufferSource, ParticleRenderType particleType, Queue<Particle> particles, Frustum frustum, CallbackInfo ci) {
		VidLibParticleRenderTypes.vl$renderParticleTypePre(particleType);
	}

	@Redirect(method = "renderParticleType(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/particle/ParticleRenderType;Ljava/util/Queue;Lnet/minecraft/client/renderer/culling/Frustum;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V"))
	private static void vl$renderParticle(Particle instance, VertexConsumer vertexConsumer, Camera camera, float v) {
		if (VidLibParticleRenderTypes.addDefaultParticle(instance)) {
			instance.render(vertexConsumer, camera, v);
		}
	}

	@Inject(method = "renderParticleType(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/particle/ParticleRenderType;Ljava/util/Queue;Lnet/minecraft/client/renderer/culling/Frustum;)V", at = @At("RETURN"))
	private static void vl$renderParticleTypePost(Camera camera, float partialTick, MultiBufferSource.BufferSource bufferSource, ParticleRenderType particleType, Queue<Particle> particles, Frustum frustum, CallbackInfo ci) {
		VidLibParticleRenderTypes.vl$renderParticleTypePost(camera, partialTick, bufferSource, particleType);
	}

	@Override
	@Nullable
	public SpriteSet getSpriteSet(ResourceLocation id) {
		return spriteSets.get(id);
	}
}
