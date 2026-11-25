package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import dev.latvian.mods.vidlib.core.api.client.VLGameRenderer;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = GameRenderer.class, priority = 1002)
public abstract class GameRendererMixin implements VLGameRenderer {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private List<Identifier> requestedPostEffects;

	@Unique
	private boolean vl$bobViewport;

	/**
	 * 1.21.5 redirected {@code LivingEntity.getHurtDir()} away from the hurt-camera bob.
	 * 26.3's {@code bobHurt(CameraRenderState, PoseStack)} no longer touches the entity: it
	 * reads {@code CameraEntityRenderState.hurtTime}, so that field read is the equivalent
	 * expression to neutralise.
	 */
	@ModifyExpressionValue(method = "bobHurt", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/CameraEntityRenderState;hurtTime:F"))
	private float vl$bobHurt(float original) {
		return 0F;
	}

	@Inject(method = "nightVisionScale", at = @At("HEAD"), cancellable = true)
	private static void vl$getNightVisionStrength(LivingEntity entity, float delta, CallbackInfoReturnable<Float> cir) {
		var override = ClientGameEngine.INSTANCE.overrideNightVisionScale(entity, delta);

		if (override != null) {
			cir.setReturnValue(override);
		}
	}

	@Inject(method = "renderLevel", at = @At("HEAD"), cancellable = true)
	private void vl$renderLevelHead(CallbackInfo ci) {
		if (minecraft.getWindow().isInvisible()) {
			ci.cancel();
		}
	}

	@Inject(method = "shouldRenderBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"), cancellable = true)
	private void vl$hideGui(CallbackInfoReturnable<Boolean> cir) {
		if (ClientGameEngine.INSTANCE.hideBlockOutline(minecraft)) {
			cir.setReturnValue(false);
		}
	}

	@WrapOperation(method = "render3dHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lcom/mojang/renderpearl/api/textures/GpuTextureView;)V"))
	private void vl$render(GameRenderer instance, CameraRenderState cameraState, PlayerRenderState playerState, GpuTextureView depthTextureView, Operation<Void> original) {
		if (ClientGameEngine.INSTANCE.shouldRenderHand(minecraft)) {
			original.call(instance, cameraState, playerState, depthTextureView);
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$bobViewport(CallbackInfo ci) {
		vl$bobViewport = true;
	}

	@Inject(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$bobItem(CameraRenderState cameraState, PlayerRenderState playerState, com.mojang.renderpearl.api.textures.GpuTextureView target, CallbackInfo ci) {
		vl$bobViewport = false;
	}

	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	private void vl$bobView(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
		if (vl$bobViewport) {
			ci.cancel();
		}
	}

	@Inject(method = "update", at = @At("RETURN"))
	private void vl$update(DeltaTracker deltaTracker, CallbackInfo ci) {
		Canvas.appendPostEffects(minecraft, requestedPostEffects);
	}
}
