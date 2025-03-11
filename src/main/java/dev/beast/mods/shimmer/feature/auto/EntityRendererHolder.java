package dev.beast.mods.shimmer.feature.auto;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

public record EntityRendererHolder<T extends Entity>(Supplier<EntityType<T>> type, EntityRendererProvider<T> renderer) {
}
