package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.core.api.client.VLGameRenderer;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.LivingEntity;
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

	@Shadow
	@Final
	private Camera mainCamera;

	@Shadow
	@Final
	private com.mojang.blaze3d.pipeline.RenderTarget mainRenderTarget;

	@Shadow
	@Final
	private com.mojang.blaze3d.pipeline.RenderTarget hud3DTarget;

	@Shadow
	protected abstract void renderItemInHand(net.minecraft.client.renderer.state.level.CameraRenderState cameraState, net.minecraft.client.renderer.state.level.PlayerRenderState playerState, com.mojang.renderpearl.api.textures.GpuTextureView target);

	@Unique
	private boolean bobViewport;

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

	/**
	 * 1.21.5 replaced the {@code Options.hideGui} read across {@code render},
	 * {@code renderItemInHand} and {@code shouldRenderBlockOutline}. 26.3 no longer reads that
	 * option from the render path - the only remaining equivalent read is
	 * {@code Hud.isHidden()} inside {@code shouldRenderBlockOutline} - so the engine decision
	 * now only has to replace that one.
	 */
	@ModifyExpressionValue(method = "shouldRenderBlockOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;isHidden()Z"))
	private boolean vl$hideGui(boolean original) {
		return ClientGameEngine.INSTANCE.hideGui(minecraft);
	}

	@Inject(method = "render3dHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lcom/mojang/renderpearl/api/textures/GpuTextureView;)V", shift = At.Shift.BY))
	private void vl$render(CameraRenderState cameraState, PlayerRenderState playerState, net.minecraft.client.renderer.state.OptionsRenderState optionsState, boolean hud3d, CallbackInfo ci) {
		if (minecraft.isGameLoadFinished() && this.minecraft.level != null && ClientGameEngine.INSTANCE.shouldRenderHand(minecraft)) {
			var profilerfiller = Profiler.get();
			profilerfiller.push("hand");
			renderItemInHand(cameraState, playerState, (hud3d ? hud3DTarget : mainRenderTarget).getDepthTextureView());
			profilerfiller.pop();
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$bobViewport(CallbackInfo ci) {
		bobViewport = true;
	}

	@Inject(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void vl$bobItem(CameraRenderState cameraState, PlayerRenderState playerState, com.mojang.renderpearl.api.textures.GpuTextureView target, CallbackInfo ci) {
		bobViewport = false;
	}

	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	private void vl$bobView(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
		if (bobViewport) {
			ci.cancel();
		}
	}
}
