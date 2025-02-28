package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.feature.entity.filter.EntityAndFilter;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.feature.entity.filter.EntityNotFilter;
import dev.beast.mods.shimmer.feature.entity.filter.EntityOrFilter;
import dev.beast.mods.shimmer.feature.entity.filter.EntityTagFilter;
import dev.beast.mods.shimmer.feature.entity.filter.EntityTypeFilter;
import dev.beast.mods.shimmer.feature.entity.filter.EntityTypeTagFilter;
import dev.beast.mods.shimmer.feature.entity.filter.EntityXorFilter;
import dev.beast.mods.shimmer.feature.zone.BlockZoneShape;
import dev.beast.mods.shimmer.feature.zone.BoxZoneShape;
import dev.beast.mods.shimmer.feature.zone.EmptyZoneShape;
import dev.beast.mods.shimmer.feature.zone.SphereZoneShape;
import dev.beast.mods.shimmer.feature.zone.UniverseZoneShape;
import dev.beast.mods.shimmer.feature.zone.ZoneShape;
import dev.beast.mods.shimmer.feature.zone.ZoneShapeGroup;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
	@SubscribeEvent
	public static void afterLoad(FMLLoadCompleteEvent event) {
		EntityFilter.REGISTRY.register(EntityNotFilter.TYPE);
		EntityFilter.REGISTRY.register(EntityAndFilter.TYPE);
		EntityFilter.REGISTRY.register(EntityOrFilter.TYPE);
		EntityFilter.REGISTRY.register(EntityXorFilter.TYPE);

		EntityFilter.REGISTRY.register(EntityFilter.NONE);
		EntityFilter.REGISTRY.register(EntityFilter.ALL);
		EntityFilter.REGISTRY.register(EntityFilter.ALIVE);
		EntityFilter.REGISTRY.register(EntityFilter.DEAD);
		EntityFilter.REGISTRY.register(EntityFilter.LIVING);
		EntityFilter.REGISTRY.register(EntityFilter.PLAYER);
		EntityFilter.REGISTRY.register(EntityFilter.SURVIVAL_PLAYER);
		EntityFilter.REGISTRY.register(EntityFilter.SURVIVAL_LIKE_PLAYER);
		EntityFilter.REGISTRY.register(EntityFilter.SPECTATOR);
		EntityFilter.REGISTRY.register(EntityFilter.CREATIVE);
		EntityFilter.REGISTRY.register(EntityFilter.SPECTATOR_OR_CREATIVE);
		EntityFilter.REGISTRY.register(EntityFilter.ITEM);
		EntityFilter.REGISTRY.register(EntityFilter.PROJECTILE);

		EntityFilter.REGISTRY.register(EntityTagFilter.TYPE);
		EntityFilter.REGISTRY.register(EntityTypeFilter.TYPE);
		EntityFilter.REGISTRY.register(EntityTypeTagFilter.TYPE);

		ZoneShape.REGISTRY.register(EmptyZoneShape.TYPE);
		ZoneShape.REGISTRY.register(UniverseZoneShape.TYPE);
		ZoneShape.REGISTRY.register(ZoneShapeGroup.TYPE);
		ZoneShape.REGISTRY.register(BlockZoneShape.TYPE);
		ZoneShape.REGISTRY.register(BoxZoneShape.TYPE);
		ZoneShape.REGISTRY.register(SphereZoneShape.TYPE);
	}
}
