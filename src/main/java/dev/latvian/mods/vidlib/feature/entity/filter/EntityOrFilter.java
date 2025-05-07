package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.world.entity.Entity;

import java.util.List;

public record EntityOrFilter(List<EntityFilter> filters) implements EntityFilter {
	public static SimpleRegistryType<EntityOrFilter> TYPE = SimpleRegistryType.dynamic("or", RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityFilter.CODEC.listOf().fieldOf("filters").forGetter(EntityOrFilter::filters)
	).apply(instance, EntityOrFilter::new)), EntityFilter.STREAM_CODEC.list().map(EntityOrFilter::new, EntityOrFilter::filters));

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
