package dev.latvian.mods.vidlib.core;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.feature.canvas.BossRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;

public interface VLEntityRenderer<T extends Entity, S extends EntityRenderState> {
	default EntityRenderer<T, S> vl$self() {
		return (EntityRenderer<T, S>) this;
	}

	default void renderModel(T entity, PoseStack ms, MultiBufferSource buffers, double xOffset, double yOffset, double zOffset, float delta, int light) {
		var state = vl$self().createRenderState(entity, delta);
		state.nameTag = null;
		state.nameTagAttachment = null;

		if (state instanceof LivingEntityRenderState s) {
			s.customName = null;
		}

		var renderOffset = vl$self().getRenderOffset(state);
		var x = xOffset + renderOffset.x();
		var y = yOffset + renderOffset.y();
		var z = zOffset + renderOffset.z();
		ms.pushPose();
		ms.translate(x, y, z);
		renderModel(state, ms, buffers, light);
		ms.popPose();
	}

	default void renderModel(S state, PoseStack ms, MultiBufferSource buffers, int light) {
		vl$self().render(state, ms, buffers, light);
	}

	default void renderBoss(T entity, PoseStack ms, MultiBufferSource buffers, double xOffset, double yOffset, double zOffset, float delta) {
		renderModel(entity, ms, BossRenderTypes.override(buffers), xOffset, yOffset, zOffset, delta, LightTexture.FULL_BRIGHT);
	}
}
