package dev.latvian.mods.vidlib.core.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.klib.math.AABBs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public interface VLEntityRenderer<T extends Entity, S extends EntityRenderState> {
	default EntityRenderer<T, S> vl$self() {
		return (EntityRenderer<T, S>) this;
	}

	/**
	 * 26.3 split entity rendering into extraction ({@code extractRenderState}) and submission
	 * ({@code submit}), so the old {@code render(S, PoseStack, MultiBufferSource, int)} entry
	 * point no longer exists. Geometry is submitted through a {@link SubmitNodeCollector} and the
	 * camera state comes from the live frame's render state.
	 */
	default void renderModel(T entity, PoseStack ms, SubmitNodeCollector buffers, double xOffset, double yOffset, double zOffset, float delta, int light) {
		var state = vl$self().createRenderState(entity, delta);
		state.nameTag = null;
		state.nameTagAttachment = null;
		state.scoreText = null;

		var renderOffset = vl$self().getRenderOffset(state);
		var x = xOffset + renderOffset.x();
		var y = yOffset + renderOffset.y();
		var z = zOffset + renderOffset.z();
		ms.pushPose();
		ms.translate(x, y, z);
		renderModel(state, ms, buffers, light);
		ms.popPose();
	}

	default CameraRenderState vl$cameraRenderState() {
		var mc = Minecraft.getInstance();
		return mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
	}

	default void renderModel(S state, PoseStack ms, SubmitNodeCollector buffers, int light) {
		vl$self().submit(state, ms, buffers, vl$cameraRenderState());
	}

	default AABB vl$getBoundingBoxForCulling(T entity, float partialTicks) {
		return AABBs.INFINITE;
	}
}
