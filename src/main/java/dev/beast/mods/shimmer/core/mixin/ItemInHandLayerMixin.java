package dev.beast.mods.shimmer.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.beast.mods.shimmer.ShimmerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel> {
	@Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;FF)V", at = @At("HEAD"), cancellable = true)
	private void shimmer$render(PoseStack poseStack, MultiBufferSource buffers, int light, S state, float yRot, float xRot, CallbackInfo ci) {
		if (ShimmerConfig.limitHeldItemRendering) {
			var pos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

			if (pos.distanceToSqr(state.x, state.y, state.z) > ShimmerConfig.heldItemRenderDistance * ShimmerConfig.heldItemRenderDistance) {
				ci.cancel();
			}
		}
	}
}
