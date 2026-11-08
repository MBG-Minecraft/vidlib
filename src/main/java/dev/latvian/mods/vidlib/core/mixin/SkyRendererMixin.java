package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.commands.RenderPass;
import dev.latvian.mods.vidlib.feature.atmosphere.ClientAtmosphere;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
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
	private void vl$renderSunMoonAndStarsHead(RenderPass renderPass, PoseStack poseStack, float sunAngle, float moonAngle, float starAngle, MoonPhase moonPhase, float rainBrightness, float starBrightness, CallbackInfo ci, @Share("clientAtmosphereOverride") LocalRef<ClientAtmosphere> override) {
		var mc = Minecraft.getInstance();
		var player = mc.player;
		var session = ClientSessionData.of(mc);
		var atmosphereRef = ClientGameEngine.INSTANCE.getAtmosphere(mc);
		override.set(player != null && atmosphereRef != null ? session.getAtmosphere(atmosphereRef) : null);
	}

	@WrapOperation(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotate(Lcom/mojang/math/Axis;F)V", ordinal = 0))
	private void vl$renderSunMoonAndStars(PoseStack ms, Axis axis, float angle, Operation<Void> original, @Share("clientAtmosphereOverride") LocalRef<ClientAtmosphere> override) {
		var o = override.get();

		if (o != null && o.celestialRotation != null) {
			ms.rotate(Axis.YP.rotation(o.celestialRotation.yawRad()));
			ms.rotate(Axis.XP.rotation(o.celestialRotation.pitchRad()));
		} else {
			original.call(ms, axis, angle);
		}
	}

	@WrapOperation(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSun(Lcom/mojang/renderpearl/api/commands/RenderPass;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$renderSun(SkyRenderer instance, RenderPass renderPass, float rainBrightness, PoseStack poseStack, Operation<Void> original, @Share("clientAtmosphereOverride") LocalRef<ClientAtmosphere> override) {
		var o = override.get();

		if (o == null || o.sun) {
			original.call(instance, renderPass, rainBrightness, poseStack);
		}
	}

	@WrapOperation(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderMoon(Lcom/mojang/renderpearl/api/commands/RenderPass;Lnet/minecraft/world/level/MoonPhase;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$renderMoon(SkyRenderer instance, RenderPass renderPass, MoonPhase moonPhase, float rainBrightness, PoseStack poseStack, Operation<Void> original, @Share("clientAtmosphereOverride") LocalRef<ClientAtmosphere> override) {
		var o = override.get();

		if (o == null || o.moon) {
			original.call(instance, renderPass, moonPhase, rainBrightness, poseStack);
		}
	}
}
