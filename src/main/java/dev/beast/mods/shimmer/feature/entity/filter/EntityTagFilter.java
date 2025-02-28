package dev.beast.mods.shimmer.feature.entity.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;

import java.util.List;

public record EntityTagFilter(List<String> tags) implements EntityFilter {
	public static SimpleRegistryType<EntityTagFilter> TYPE = SimpleRegistryType.dynamic(Shimmer.id("tags"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.listOf().fieldOf("tags").forGetter(EntityTagFilter::tags)
	).apply(instance, EntityTagFilter::new)), ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).map(EntityTagFilter::new, EntityTagFilter::tags));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		for (var tag : tags) {
			if (!entity.getTags().contains(tag)) {
				return false;
			}
		}

		return true;
	}
}
