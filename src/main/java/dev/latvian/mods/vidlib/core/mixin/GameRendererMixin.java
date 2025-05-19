package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.latvian.mods.vidlib.core.VLGameRenderer;
import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
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

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements VLGameRenderer {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private float renderDistance;

	@Shadow
	@Final
	private Camera mainCamera;

	@Unique
	private Matrix4f frustumMatrix;

	@Shadow
	private boolean renderHand;

	@Redirect(method = "bobHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getHurtDir()F"))
	private float vl$bobHurt(LivingEntity entity) {
		return 0F;
	}

	@Inject(method = "getNightVisionScale", at = @At("HEAD"), cancellable = true)
	private static void vl$getNightVisionStrength(LivingEntity entity, float delta, CallbackInfoReturnable<Float> cir) {
		int duration = entity.getEffect(MobEffects.NIGHT_VISION).getDuration();
		cir.setReturnValue(duration > 20F ? 1F : duration / 20F);
	}

	/**
	 * @author Lat
	 * @reason Extend depth
	 */
	@Overwrite
	public float getDepthFar() {
		return MiscClientUtils.depthFar(renderDistance);
	}

	@ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"))
	private boolean vl$renderHand(boolean original) {
		return false;
	}

	@Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lnet/minecraft/world/phys/Vec3;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"))
	private void vl$getFrustumMatrix(LevelRenderer instance, Vec3 cameraPosition, Matrix4f fMatrix, Matrix4f projectionMatrix) {
		instance.prepareCullFrustum(cameraPosition, fMatrix, projectionMatrix);
		frustumMatrix = fMatrix;
	}

	@Shadow
	protected abstract void renderItemInHand(Camera camera, float partialTick, Matrix4f projectionMatrix);

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;setOrtho(FFFFFF)Lorg/joml/Matrix4f;"))
	private void vl$render(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
		if (minecraft.isGameLoadFinished() && renderLevel && this.minecraft.level != null && renderHand && CameraOverride.get(minecraft) == null) {
			var profilerfiller = Profiler.get();
			profilerfiller.push("hand");
			renderItemInHand(mainCamera, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true), frustumMatrix);
			profilerfiller.pop();
		}
	}
}
