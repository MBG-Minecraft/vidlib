package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import dev.latvian.mods.vidlib.core.api.client.VLSkyRenderState;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
	@Unique
	private VLSkyRenderState vl$skyState;

	@ModifyReturnValue(method = "shouldRenderDarkDisc", at = @At("RETURN"))
	private boolean vl$shouldRenderDarkDisc(boolean original, @Local(argsOnly = true, name = "deltaPartialTick") float deltaPartialTick, @Local(argsOnly = true, name = "level") ClientLevel level) {
		return ClientGameEngine.INSTANCE.shouldRenderDarkDisc(deltaPartialTick, level, original);
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void vl$renderHead(GpuBufferSlice skyFog, SkyRenderState state, CallbackInfo ci) {
		vl$skyState = (VLSkyRenderState) state;
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void vl$renderReturn(GpuBufferSlice skyFog, SkyRenderState state, CallbackInfo ci) {
		vl$skyState = null;
	}

	@WrapOperation(method = "renderSunMoonAndStars", at = {
		@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotate(Lcom/mojang/math/Axis;F)V", ordinal = 0),
		@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotate(Lcom/mojang/math/Axis;F)V", ordinal = 1)
	})
	private void vl$renderSunMoonAndStars(PoseStack ms, Axis axis, float angle, Operation<Void> original) {
		var tilt = vl$skyState.vl$celestialTilt();

		if (tilt != null) {
			ms.rotate(tilt);
		}
	}

	@WrapOperation(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSun(Lcom/mojang/renderpearl/api/commands/RenderPass;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$renderSun(SkyRenderer instance, RenderPass renderPass, float rainBrightness, PoseStack poseStack, Operation<Void> original) {
		if (vl$skyState.vl$sun()) {
			original.call(instance, renderPass, rainBrightness, poseStack);
		}
	}

	@WrapOperation(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderMoon(Lcom/mojang/renderpearl/api/commands/RenderPass;Lnet/minecraft/world/level/MoonPhase;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$renderMoon(SkyRenderer instance, RenderPass renderPass, MoonPhase moonPhase, float rainBrightness, PoseStack poseStack, Operation<Void> original) {
		if (vl$skyState.vl$moon()) {
			original.call(instance, renderPass, moonPhase, rainBrightness, poseStack);
		}
	}
}
