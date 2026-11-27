package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import dev.latvian.mods.vidlib.core.api.client.VLSkyRenderState;
import dev.latvian.mods.vidlib.feature.atmosphere.SkyboxRenderer;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.MoonPhase;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
	@Unique
	private SkyRenderState vl$skyState;

	@ModifyReturnValue(method = "shouldRenderDarkDisc", at = @At("RETURN"))
	private boolean vl$shouldRenderDarkDisc(boolean original, @Local(argsOnly = true, name = "deltaPartialTick") float deltaPartialTick, @Local(argsOnly = true, name = "level") ClientLevel level) {
		return ClientGameEngine.INSTANCE.shouldRenderDarkDisc(deltaPartialTick, level, original);
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void vl$renderHead(GpuBufferSlice skyFog, SkyRenderState state, CallbackInfo ci) {
		vl$skyState = state;
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void vl$renderReturn(GpuBufferSlice skyFog, SkyRenderState state, CallbackInfo ci) {
		vl$skyState = null;
	}

	@Inject(method = "renderSunMoonAndStars", at = {
		@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotate(Lcom/mojang/math/Axis;F)V", ordinal = 0),
		@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotate(Lcom/mojang/math/Axis;F)V", ordinal = 1)
	})
	private void vl$renderSunMoonAndStars(RenderPass renderPass, PoseStack ms, float sunAngle, float moonAngle, float starAngle, MoonPhase moonPhase, float rainBrightness, float starBrightness, CallbackInfo ci) {
		ms.rotate(((VLSkyRenderState) vl$skyState).vl$state().celestialTilt);
	}

	@WrapOperation(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSun(Lcom/mojang/renderpearl/api/commands/RenderPass;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$renderSun(SkyRenderer instance, RenderPass renderPass, float rainBrightness, PoseStack poseStack, Operation<Void> original) {
		if (((VLSkyRenderState) vl$skyState).vl$state().sun) {
			original.call(instance, renderPass, rainBrightness, poseStack);
		}
	}

	@WrapOperation(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderMoon(Lcom/mojang/renderpearl/api/commands/RenderPass;Lnet/minecraft/world/level/MoonPhase;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$renderMoon(SkyRenderer instance, RenderPass renderPass, MoonPhase moonPhase, float rainBrightness, PoseStack poseStack, Operation<Void> original) {
		if (((VLSkyRenderState) vl$skyState).vl$state().moon) {
			original.call(instance, renderPass, moonPhase, rainBrightness, poseStack);
		}
	}

	@Inject(method = "renderSunriseAndSunset", at = @At("HEAD"), cancellable = true)
	private void vl$skybox(RenderPass renderPass, PoseStack poseStack, float sunAngle, Vector4fc sunriseAndSunsetColor, CallbackInfo ci) {
		if (SkyboxRenderer.render(renderPass, vl$skyState)) {
			ci.cancel();
		}
	}
}
