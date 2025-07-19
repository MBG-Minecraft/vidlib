package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.entity.EntityTypeImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record EntityTypeFilter(EntityType<?> entityType) implements EntityFilter {
	public static SimpleRegistryType<EntityTypeFilter> TYPE = SimpleRegistryType.dynamic("entity_type", RecordCodecBuilder.mapCodec(instance -> instance.group(
		BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_type").forGetter(EntityTypeFilter::entityType)
	).apply(instance, EntityTypeFilter::new)), ByteBufCodecs.registry(Registries.ENTITY_TYPE).map(EntityTypeFilter::new, EntityTypeFilter::entityType));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = new ImBuilderHolder<>("Type", Builder::new);

		public final EntityTypeImBuilder entityType = new EntityTypeImBuilder(EntityType.PLAYER);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return entityType.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return entityType.isValid();
		}

		@Override
		public EntityFilter build() {
			return new EntityTypeFilter(entityType.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return entity.getType() == entityType;
	}
}
