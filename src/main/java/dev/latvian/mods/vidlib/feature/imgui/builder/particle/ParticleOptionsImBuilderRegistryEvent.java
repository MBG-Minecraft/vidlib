package dev.latvian.mods.vidlib.feature.imgui.builder.particle;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderEvent;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParticleOptionsImBuilderRegistryEvent extends ImBuilderEvent<ParticleOptions> {
	private final Map<ParticleType<?>, ParticleOptionsImBuilder.Factory<?>> map;

	ParticleOptionsImBuilderRegistryEvent() {
		this.map = new LinkedHashMap<>();

		for (var type : BuiltInRegistries.PARTICLE_TYPE) {
			if (type instanceof ParticleOptions options) {
				map.put(type, t -> new ParticleOptionsImBuilder.Simple(options));
			} else {
				map.put(type, ParticleOptionsImBuilder.NBT::new);
			}
		}
	}

	public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleOptionsImBuilder.Factory<T> builder) {
		map.put(type, builder);
	}

	public <T extends ParticleOptions> void register(List<ParticleType<T>> types, ParticleOptionsImBuilder.Factory<T> builder) {
		for (var type : types) {
			register(type, builder);
		}
	}

	@Override
	public Collection<ImBuilderHolder<ParticleOptions>> getBuilderHolders() {
		var list = new ArrayList<ImBuilderHolder<ParticleOptions>>();

		for (var entry : map.entrySet()) {
			list.add(new ImBuilderHolder<>(BuiltInRegistries.PARTICLE_TYPE.getKey(entry.getKey()).toString(), () -> entry.getValue().create(Cast.to(entry.getKey()))));
		}

		return list;
	}
}