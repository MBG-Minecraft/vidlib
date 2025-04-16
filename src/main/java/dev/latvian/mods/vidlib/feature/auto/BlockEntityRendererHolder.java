package dev.latvian.mods.vidlib.feature.auto;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.ApiStatus;

public record BlockEntityRendererHolder<T extends BlockEntity>(DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<? extends T>> type, BlockEntityRendererProvider<T> renderer) {
	@ApiStatus.Internal
	public void register(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(type().get(), renderer());
	}
}
