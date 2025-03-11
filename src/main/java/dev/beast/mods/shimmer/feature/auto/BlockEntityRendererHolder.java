package dev.beast.mods.shimmer.feature.auto;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public record BlockEntityRendererHolder<T extends BlockEntity>(Supplier<BlockEntityType<T>> type, BlockEntityRendererProvider<T> renderer) {
}
