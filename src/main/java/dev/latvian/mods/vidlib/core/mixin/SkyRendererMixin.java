package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.latvian.mods.vidlib.feature.atmosphere.ClientAtmosphere;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
	@ModifyReturnValue(method = "shouldRenderDarkDisc", at = @At("RETURN"))
	private boolean vl$shouldRenderDarkDisc(boolean original, @Local(argsOnly = true, name = "deltaPartialTick") float deltaPartialTick, @Local(argsOnly = true, name = "level") ClientLevel level) {
		return ClientGameEngine.INSTANCE.shouldRenderDarkDisc(deltaPartialTick, level, original);
	}

	@Inject(method = "renderSunMoonAndStars", at = @At("HEAD"))
	private void vl$renderSunMoonAndStarsHead(PoseStack poseStack, float sunAngle, float moonAngle, float starAngle, MoonPhase moonPhase, float rainBrightness, float starBrightness, CallbackInfo ci, @Share("clientAtmosphereOverride") LocalRef<ClientAtmosphere> override) {
		var player = Minecraft.getInstance().player;
		override.set(player != null ? player.vl$sessionData().atmosphere : null);
	}

	@WrapOperation(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V", ordinal = 1))
	private void vl$renderSunMoonAndStars(PoseStack ms, Quaternionfc by, Operation<Void> original, @Share("clientAtmosphereOverride") LocalRef<ClientAtmosphere> override) {
		var o = override.get();

		if (o != null && o.celestialRotation != null) {
			ms.mulPose(Axis.YP.rotation(o.celestialRotation.yawRad()));
			ms.mulPose(Axis.XP.rotation(o.celestialRotation.pitchRad()));
		} else {
			original.call(ms, by);
		}
	}

	@WrapOperation(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSun(FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$renderSun(SkyRenderer instance, float rainBrightness, PoseStack poseStack, Operation<Void> original, @Share("clientAtmosphereOverride") LocalRef<ClientAtmosphere> override) {
		var o = override.get();

		if (o == null || o.sun) {
			original.call(instance, rainBrightness, poseStack);
		}
	}

	@WrapOperation(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderMoon(Lnet/minecraft/world/level/MoonPhase;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$renderMoon(SkyRenderer instance, MoonPhase moonPhase, float rainBrightness, PoseStack poseStack, Operation<Void> original, @Share("clientAtmosphereOverride") LocalRef<ClientAtmosphere> override) {
		var o = override.get();

		if (o == null || o.moon) {
			original.call(instance, moonPhase, rainBrightness, poseStack);
		}
	}
}
