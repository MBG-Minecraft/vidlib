package dev.beast.mods.shimmer.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SkyRenderer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
	@Shadow
	protected abstract void renderStars(FogParameters fog, float starBrightness, PoseStack poseStack);

	@Redirect(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V", ordinal = 1))
	private void shimmer$renderSunMoonAndStars(PoseStack ms, Quaternionf quaternion) {
		var player = Minecraft.getInstance().player;
		var override = player != null ? player.shimmer$sessionData().skybox : null;

		if (override != null && override.data.celestialRotation().isPresent()) {
			ms.mulPose(Axis.YP.rotation(override.data.celestialRotation().get().yawRad()));
			ms.mulPose(Axis.XP.rotation(override.data.celestialRotation().get().pitchRad()));
		} else {
			ms.mulPose(quaternion);
		}
	}

	@Redirect(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderStars(Lnet/minecraft/client/renderer/FogParameters;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void shimmer$renderStarsOriginal(SkyRenderer instance, FogParameters fog, float starBrightness, PoseStack poseStack) {
		var player = Minecraft.getInstance().player;
		var override = player != null ? player.shimmer$sessionData().skybox : null;

		if (override == null || override.data.stars().isEmpty()) {
			renderStars(fog, starBrightness, poseStack);
		}
	}

	@Inject(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V", shift = At.Shift.AFTER))
	private void shimmer$renderStars(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float timeOfDay, int moonPhase, float rainLevel, float starBrightness, FogParameters fog, CallbackInfo ci) {
		var player = Minecraft.getInstance().player;
		var override = player != null ? player.shimmer$sessionData().skybox : null;

		if (override != null && override.data.stars().isPresent() && override.data.stars().get() > 0F) {
			renderStars(fog, override.data.stars().get(), poseStack);
		}
	}
}
