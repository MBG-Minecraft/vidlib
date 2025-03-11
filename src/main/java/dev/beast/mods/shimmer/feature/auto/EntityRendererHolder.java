package dev.beast.mods.shimmer.feature.auto;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public record EntityRendererHolder<T extends Entity>(DeferredHolder<EntityType<?>, EntityType<? extends T>> type, EntityRendererProvider<T> renderer) {
}
