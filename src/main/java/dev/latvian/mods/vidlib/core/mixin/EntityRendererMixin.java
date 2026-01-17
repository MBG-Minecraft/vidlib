package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.VLEntityRenderer;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityRenderer.class, priority = 1001) // Load before Sodium
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> implements VLEntityRenderer<T, S> {

	@Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
	private void vl$shouldRender(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}

	@ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;shouldShowName(Lnet/minecraft/world/entity/Entity;D)Z"))
	private boolean vl$shouldShowName(boolean original, @Local(argsOnly = true) Entity entity) {
		return ClientGameEngine.INSTANCE.shouldShowName(entity, original);
	}

	@Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At("RETURN"))
	private void vl$extractRenderState(T entity, float delta, CallbackInfoReturnable<S> cir) {
		VidLibEntityRenderStates.extract(entity, cir.getReturnValue(), delta);
	}
}
