package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.world.entity.Entity;

public record EntityXorFilter(EntityFilter a, EntityFilter b) implements EntityFilter {
	public static SimpleRegistryType<EntityXorFilter> TYPE = SimpleRegistryType.dynamic("xor", RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityFilter.CODEC.fieldOf("a").forGetter(EntityXorFilter::a),
		EntityFilter.CODEC.fieldOf("b").forGetter(EntityXorFilter::b)
	).apply(instance, EntityXorFilter::new)), CompositeStreamCodec.of(
		EntityFilter.STREAM_CODEC, EntityXorFilter::a,
		EntityFilter.STREAM_CODEC, EntityXorFilter::b,
		EntityXorFilter::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return a.test(entity) ^ b.test(entity);
	}
}
