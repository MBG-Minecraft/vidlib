package dev.latvian.mods.vidlib.feature.particle;

import net.minecraft.core.particles.ParticleType;
import net.neoforged.bus.api.Event;

import java.util.Map;
import java.util.function.Supplier;

public class ParticleOptionsImBuilderRegistryEvent extends Event {
	private final Map<ParticleType<?>, Supplier<ParticleOptionsImBuilder>> map;

	ParticleOptionsImBuilderRegistryEvent(Map<ParticleType<?>, Supplier<ParticleOptionsImBuilder>> map) {
		this.map = map;
	}

	public void register(ParticleType<?> type, Supplier<ParticleOptionsImBuilder> builder) {
		map.put(type, builder);
	}
}