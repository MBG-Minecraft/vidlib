package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState> {
	@Redirect(method = "getRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;outline(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
	private RenderType vl$outline(ResourceLocation texture, @Local(argsOnly = true) S state) {
		return state instanceof PlayerRenderState ? VidLibRenderTypes.STRONG_OUTLINE_NO_CULL.apply(texture) : RenderType.outline(texture);
	}

	@ModifyVariable(method = "getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/RenderType;", at = @At("HEAD"), argsOnly = true, ordinal = 1)
	private boolean vl$overrideRenderTranslucent(boolean renderTranslucent, @Local(argsOnly = true) S state) {
		return renderTranslucent || state instanceof PlayerRenderState && state.getRenderDataOrDefault(VidLibEntityRenderStates.TRANSLUCENT, Boolean.FALSE);
	}
}
