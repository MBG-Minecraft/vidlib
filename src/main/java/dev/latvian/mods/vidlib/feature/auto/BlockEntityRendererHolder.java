package dev.latvian.mods.vidlib.feature.auto;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.ApiStatus;

public record BlockEntityRendererHolder<T extends BlockEntity, S extends BlockEntityRenderState>(DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<? extends T>> type, BlockEntityRendererProvider<T, S> renderer) {
	@ApiStatus.Internal
	public void register(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(type().get(), renderer());
	}
}
