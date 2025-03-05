package dev.beast.mods.shimmer.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;

import java.util.List;

public record EntityOrFilter(List<EntityFilter> filters) implements EntityFilter {
	public static SimpleRegistryType<EntityOrFilter> TYPE = SimpleRegistryType.dynamic(Shimmer.id("or"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityFilter.CODEC.listOf().fieldOf("filters").forGetter(EntityOrFilter::filters)
	).apply(instance, EntityOrFilter::new)), EntityFilter.STREAM_CODEC.apply(ByteBufCodecs.list()).map(EntityOrFilter::new, EntityOrFilter::filters));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		for (var filter : filters) {
			if (filter.test(entity)) {
				return true;
			}
		}

		return false;
	}
}
