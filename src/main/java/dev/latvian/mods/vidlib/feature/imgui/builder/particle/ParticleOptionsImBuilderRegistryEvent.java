package dev.latvian.mods.vidlib.feature.imgui.builder.particle;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.bus.api.Event;

import java.util.List;
import java.util.Map;

public class ParticleOptionsImBuilderRegistryEvent extends Event {
	private final Map<ParticleType<?>, ParticleOptionsImBuilder.Factory<?>> map;

	ParticleOptionsImBuilderRegistryEvent(Map<ParticleType<?>, ParticleOptionsImBuilder.Factory<?>> map) {
		this.map = map;
	}

	public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleOptionsImBuilder.Factory<T> builder) {
		map.put(type, builder);
	}

	public <T extends ParticleOptions> void register(List<ParticleType<T>> types, ParticleOptionsImBuilder.Factory<T> builder) {
		for (var type : types) {
			register(type, builder);
		}
	}
}