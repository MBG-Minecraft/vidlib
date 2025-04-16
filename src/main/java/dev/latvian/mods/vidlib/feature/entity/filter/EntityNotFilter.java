package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.world.entity.Entity;

public record EntityNotFilter(EntityFilter filter) implements EntityFilter {
	public static SimpleRegistryType<EntityNotFilter> TYPE = SimpleRegistryType.dynamic(VidLib.id("not"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityFilter.CODEC.fieldOf("filter").forGetter(EntityNotFilter::filter)
	).apply(instance, EntityNotFilter::new)), EntityFilter.STREAM_CODEC.map(EntityNotFilter::new, EntityNotFilter::filter));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return !filter.test(entity);
	}
}
