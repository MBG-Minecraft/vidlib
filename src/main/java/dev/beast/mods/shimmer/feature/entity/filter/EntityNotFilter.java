package dev.beast.mods.shimmer.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.world.entity.Entity;

public record EntityNotFilter(EntityFilter filter) implements EntityFilter {
	public static SimpleRegistryType<EntityNotFilter> TYPE = SimpleRegistryType.dynamic(Shimmer.id("not"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityFilter.REGISTRY.valueCodec().fieldOf("filter").forGetter(EntityNotFilter::filter)
	).apply(instance, EntityNotFilter::new)), EntityFilter.REGISTRY.valueStreamCodec().map(EntityNotFilter::new, EntityNotFilter::filter));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return !filter.test(entity);
	}
}
