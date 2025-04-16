package dev.latvian.mods.vidlib.feature.auto;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.ApiStatus;

public record EntityRendererHolder<T extends Entity>(DeferredHolder<EntityType<?>, ? extends EntityType<? extends T>> type, EntityRendererProvider<T> renderer) {
	@ApiStatus.Internal
	public void register(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(type().get(), renderer());
	}
}
