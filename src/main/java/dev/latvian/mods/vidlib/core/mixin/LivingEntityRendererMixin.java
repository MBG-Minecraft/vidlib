package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
	@ModifyVariable(method = "getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;", at = @At("HEAD"), argsOnly = true, ordinal = 1)
	private boolean vl$overrideRenderTranslucent(boolean renderTranslucent, @Local(argsOnly = true) LivingEntityRenderState state) {
		return renderTranslucent || vl$isForceTranslucent(state);
	}

	@ModifyVariable(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("STORE"), ordinal = 1)
	private boolean vl$renderTranslucentFlag(boolean flag, @Local(argsOnly = true) LivingEntityRenderState state) {
		return flag || vl$isForceTranslucent(state);
	}

	@Shadow
	@Final
	protected List<?> layers;

	@Unique
	private static boolean vl$isForceTranslucent(LivingEntityRenderState state) {
		return state instanceof AvatarRenderState as && as.getRenderDataOrDefault(VidLibEntityRenderStates.TRANSLUCENT, Boolean.FALSE);
	}
}
