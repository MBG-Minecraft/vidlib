package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.core.VLGameRenderer;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameRenderer.class, priority = 1002)
public abstract class GameRendererMixin implements VLGameRenderer {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private float renderDistance;

	@Shadow
	@Final
	private Camera mainCamera;

	@Shadow
	private boolean renderHand;

	@Unique
	private boolean bobViewport;

	@Redirect(method = "bobHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getHurtDir()F"))
	private float vl$bobHurt(LivingEntity entity) {
		return 0F;
	}

	@Inject(method = "getNightVisionScale", at = @At("HEAD"), cancellable = true)
	private static void vl$getNightVisionStrength(LivingEntity entity, float delta, CallbackInfoReturnable<Float> cir) {
		var override = ClientGameEngine.INSTANCE.overrideNightVisionScale(entity, delta);

		if (override != null) {
			cir.setReturnValue(override);
		}
	}

	/**
	 * @author Lat
	 * @reason Extend depth
	 */
	@Overwrite
	public float getDepthFar() {
		return ClientGameEngine.INSTANCE.getFarDepth(renderDistance);
	}

	@Inject(method = "renderLevel", at = @At("HEAD"), cancellable = true)
	private void vl$renderLevelHead(DeltaTracker deltaTracker, CallbackInfo ci) {
		if (minecraft.getWindow().isInvisible()) {
			ci.cancel();
		}
	}

	@ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"))
	private boolean vl$renderHand(boolean original) {
		return false;
	}

	@Shadow
	protected abstract void renderItemInHand(Camera camera, float partialTick, Matrix4f projectionMatrix);

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;setOrtho(FFFFFF)Lorg/joml/Matrix4f;"))
	private void vl$render(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
		if (minecraft.isGameLoadFinished() && renderLevel && this.minecraft.level != null && renderHand && ClientGameEngine.INSTANCE.overrideCamera(minecraft) == null) {
			var profilerfiller = Profiler.get();
			profilerfiller.push("hand");
			renderItemInHand(mainCamera, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true), MiscClientUtils.FRUSTUM_MATRIX);
			profilerfiller.pop();
		}
	}

	@ModifyExpressionValue(method = {"render", "renderItemInHand", "shouldRenderBlockOutline"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z"))
	private boolean vl$hideGui(boolean original) {
		return ClientGameEngine.INSTANCE.hideGui(minecraft);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
	private void vl$bobViewport(DeltaTracker deltaTracker, CallbackInfo ci) {
		bobViewport = true;
	}

	@Inject(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
	private void vl$bobItem(Camera camera, float partialTick, Matrix4f projectionMatrix, CallbackInfo ci) {
		bobViewport = false;
	}

	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	private void vl$bobView(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
		if (bobViewport) {
			ci.cancel();
		}
	}
}
