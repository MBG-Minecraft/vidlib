package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record EntityTypeTagFilter(TagKey<EntityType<?>> tag) implements EntityFilter {
	public static SimpleRegistryType<EntityTypeTagFilter> TYPE = SimpleRegistryType.dynamic(VidLib.id("type_tag"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		TagKey.codec(Registries.ENTITY_TYPE).fieldOf("entity_type").forGetter(EntityTypeTagFilter::tag)
	).apply(instance, EntityTypeTagFilter::new)), ByteBufCodecs.fromCodecWithRegistries(TagKey.codec(Registries.ENTITY_TYPE)).map(EntityTypeTagFilter::new, EntityTypeTagFilter::tag));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return entity.getType().builtInRegistryHolder().is(tag);
	}
}
