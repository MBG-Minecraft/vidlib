package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.world.entity.Entity;

import java.util.List;

public record EntityAndFilter(List<EntityFilter> filters) implements EntityFilter {
	public static SimpleRegistryType<EntityAndFilter> TYPE = SimpleRegistryType.dynamic("and", RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityFilter.CODEC.listOf().fieldOf("filters").forGetter(EntityAndFilter::filters)
	).apply(instance, EntityAndFilter::new)), EntityFilter.STREAM_CODEC.listOf().map(EntityAndFilter::new, EntityAndFilter::filters));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		for (var filter : filters) {
			if (!filter.test(entity)) {
				return false;
			}
		}

		return true;
	}
}
