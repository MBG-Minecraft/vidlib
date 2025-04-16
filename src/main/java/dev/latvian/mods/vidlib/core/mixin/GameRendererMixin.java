package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.latvian.mods.vidlib.core.VLGameRenderer;
import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements VLGameRenderer {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Redirect(method = "bobHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getHurtDir()F"))
	private float vl$bobHurt(LivingEntity entity) {
		return 0F;
	}

	@Inject(method = "getNightVisionScale", at = @At("HEAD"), cancellable = true)
	private static void vl$getNightVisionStrength(LivingEntity entity, float delta, CallbackInfoReturnable<Float> cir) {
		int duration = entity.getEffect(MobEffects.NIGHT_VISION).getDuration();
		cir.setReturnValue(duration > 20F ? 1F : duration / 20F);
	}

	@ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"))
	private boolean vl$renderHand(boolean original) {
		return original && CameraOverride.get(minecraft) == null;
	}
}
