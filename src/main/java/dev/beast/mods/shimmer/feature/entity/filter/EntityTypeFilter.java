package dev.beast.mods.shimmer.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record EntityTypeFilter(EntityType<?> entityType) implements EntityFilter {
	public static SimpleRegistryType<EntityTypeFilter> TYPE = SimpleRegistryType.dynamic(Shimmer.id("entity_type"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_type").forGetter(EntityTypeFilter::entityType)
	).apply(instance, EntityTypeFilter::new)), ByteBufCodecs.registry(Registries.ENTITY_TYPE).map(EntityTypeFilter::new, EntityTypeFilter::entityType));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return entity.getType() == entityType;
	}
}
