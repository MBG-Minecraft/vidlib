package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.util.IntOrUUID;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record ExactEntityFilter(IntOrUUID entityId) implements EntityFilter {
	public static SimpleRegistryType<ExactEntityFilter> TYPE = SimpleRegistryType.dynamic("exact", RecordCodecBuilder.mapCodec(instance -> instance.group(
		IntOrUUID.DATA_TYPE.codec().fieldOf("entity_id").forGetter(ExactEntityFilter::entityId)
	).apply(instance, ExactEntityFilter::new)), IntOrUUID.DATA_TYPE.streamCodec().map(ExactEntityFilter::new, ExactEntityFilter::entityId));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return entityId.testEntity(entity);
	}

	@Override
	@Nullable
	public Entity getFirst(Level level) {
		return level.getEntity(entityId);
	}
}
