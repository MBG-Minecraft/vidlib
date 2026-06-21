package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.core.VLGameRenderer;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameRenderer.class, priority = 1002)
public abstract class GameRendererMixin implements VLGameRenderer {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private boolean vl$cancelViewportBobbing;

	@ModifyExpressionValue(method = "bobHurt", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/CameraEntityRenderState;hurtDir:F"))
	private float vl$bobHurt(float original) {
		return 0F;
	}

	@Inject(method = "getNightVisionScale", at = @At("HEAD"), cancellable = true)
	private static void vl$getNightVisionStrength(LivingEntity entity, float delta, CallbackInfoReturnable<Float> cir) {
		var override = ClientGameEngine.INSTANCE.overrideNightVisionScale(entity, delta);

		if (override != null) {
			cir.setReturnValue(override);
		}
	}

	@Override
	public float getDepthFar() {
		var renderDistance = minecraft.options.getEffectiveRenderDistance() * 16F;
		var vanillaDepthFar = Math.max(renderDistance * 4F, minecraft.options.cloudRange().get() * 16F);
		return ClientGameEngine.INSTANCE.getFarDepth(vanillaDepthFar);
	}

	@Inject(method = "renderLevel", at = @At("HEAD"), cancellable = true)
	private void vl$renderLevelHead(DeltaTracker deltaTracker, CallbackInfo ci) {
		if (minecraft.getWindow().isInvisible()) {
			ci.cancel();
		}
	}

	@ModifyExpressionValue(method = {"extractOptions", "shouldRenderBlockOutline"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z"))
	private boolean vl$hideGui(boolean original) {
		return ClientGameEngine.INSTANCE.hideGui(minecraft);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$bobViewport(DeltaTracker deltaTracker, CallbackInfo ci) {
		vl$cancelViewportBobbing = true;
	}

	@Inject(method = "renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$bobItem(CameraRenderState cameraState, float partialTick, Matrix4fc modelViewMatrix, CallbackInfo ci) {
		vl$cancelViewportBobbing = false;
	}

	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	private void vl$bobView(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
		if (vl$cancelViewportBobbing) {
			ci.cancel();
		}
	}
}
