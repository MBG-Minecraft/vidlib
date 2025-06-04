package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.VLEntityRenderer;
import dev.latvian.mods.vidlib.feature.canvas.BossRendering;
import dev.latvian.mods.vidlib.feature.client.VidLibEntityRenderStates;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> implements VLEntityRenderer<T, S> {
	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	public boolean shouldRender(T entity, Frustum camera, double camX, double camY, double camZ) {
		return true;
	}

	@ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;shouldShowName(Lnet/minecraft/world/entity/Entity;D)Z"))
	private boolean vl$shouldShowName(boolean original, @Local(argsOnly = true) Entity entity) {
		return BossRendering.active <= 0 && (original || MiscClientUtils.shouldShowName(entity));
	}

	@Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At("RETURN"))
	private void vl$extractRenderState(T entity, float delta, CallbackInfoReturnable<S> cir) {
		VidLibEntityRenderStates.extract(entity, cir.getReturnValue(), delta);
	}
}
