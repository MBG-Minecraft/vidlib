package dev.latvian.mods.vidlib.core.api.client;

import dev.latvian.mods.klib.math.AABBs;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public interface VLEntityRenderer<T extends Entity, S extends EntityRenderState> {
	default EntityRenderer<T, S> vl$self() {
		return (EntityRenderer<T, S>) this;
	}

	default AABB vl$getBoundingBoxForCulling(T entity, float partialTicks) {
		return AABBs.INFINITE;
	}
}
