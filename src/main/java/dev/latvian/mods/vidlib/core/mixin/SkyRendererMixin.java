package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
	@Shadow
	private void renderStars(float starBrightness, PoseStack poseStack) {
	}

	@Shadow
	private void renderMoon(MoonPhase moonPhase, float rainBrightness, PoseStack poseStack) {
	}

	@Shadow
	private void renderSun(float rainBrightness, PoseStack poseStack) {
	}

	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	private boolean shouldRenderDarkDisc(float partialTick, ClientLevel level) {
		return false;
	}

	@Redirect(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V", ordinal = 1))
	private void vl$renderSunMoonAndStars(PoseStack ms, Quaternionfc quaternion) {
		var player = Minecraft.getInstance().player;
		var override = player != null ? player.vl$sessionData().skybox : null;

		if (override != null && override.data.celestialRotation().isPresent()) {
			ms.mulPose(Axis.YP.rotation(override.data.celestialRotation().get().yawRad()));
			ms.mulPose(Axis.XP.rotation(override.data.celestialRotation().get().pitchRad()));
		} else {
			ms.mulPose(quaternion);
		}
	}

	@Redirect(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSun(FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$renderSun(SkyRenderer instance, float rainBrightness, PoseStack poseStack) {
		var player = Minecraft.getInstance().player;
		var override = player != null ? player.vl$sessionData().skybox : null;

		if (override == null || override.data.sun()) {
			renderSun(rainBrightness, poseStack);
		}
	}

	@Redirect(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderMoon(Lnet/minecraft/world/level/MoonPhase;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$renderMoon(SkyRenderer instance, MoonPhase moonPhase, float rainBrightness, PoseStack poseStack) {
		var player = Minecraft.getInstance().player;
		var override = player != null ? player.vl$sessionData().skybox : null;

		if (override == null || override.data.moon()) {
			renderMoon(moonPhase, rainBrightness, poseStack);
		}
	}

	@Redirect(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderStars(FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$renderStarsOriginal(SkyRenderer instance, float starBrightness, PoseStack poseStack) {
		var player = Minecraft.getInstance().player;
		var override = player != null ? player.vl$sessionData().skybox : null;

		if (override == null || override.data.stars().isEmpty()) {
			renderStars(starBrightness, poseStack);
		} else if (override.data.stars().get() > 0F) {
			renderStars(override.data.stars().get(), poseStack);
		}
	}
}
