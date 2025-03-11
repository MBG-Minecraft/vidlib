package dev.beast.mods.shimmer.feature.auto;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public record BlockEntityRendererHolder<T extends BlockEntity>(DeferredHolder<BlockEntityType<?>, BlockEntityType<? extends T>> type, BlockEntityRendererProvider<T> renderer) {
}
