package dev.beast.mods.shimmer.core.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TrackingEmitter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

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

		for (var entry : particles.entrySet()) {
			level.getProfiler().push(entry.getKey().toString());
			tickParticleList(entry.getValue());
			level.getProfiler().pop();
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
}
