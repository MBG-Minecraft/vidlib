package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;

import java.util.List;

public record EntityTagFilter(List<String> tags) implements EntityFilter {
	public static SimpleRegistryType<EntityTagFilter> TYPE = SimpleRegistryType.dynamic("tags", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.listOf().fieldOf("tags").forGetter(EntityTagFilter::tags)
	).apply(instance, EntityTagFilter::new)), ByteBufCodecs.STRING_UTF8.list().map(EntityTagFilter::new, EntityTagFilter::tags));

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
