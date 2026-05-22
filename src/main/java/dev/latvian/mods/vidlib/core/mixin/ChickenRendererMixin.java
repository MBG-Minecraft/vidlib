package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.client.babymodel.BabyChickenModel;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChickenRenderer.class)
public class ChickenRendererMixin {
	@Redirect(method = "bakeModels", at = @At(value = "NEW", target = "(Lnet/minecraft/client/model/geom/ModelPart;)Lnet/minecraft/client/model/ChickenModel;", ordinal = 1))
	private static ChickenModel vl$bakeModels(ModelPart root) {
		return new BabyChickenModel(root);
	}

	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/ChickenRenderState;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
	public void vl$getTextureLocation(ChickenRenderState state, CallbackInfoReturnable<ResourceLocation> cir) {
		if (state.isBaby) {
			cir.setReturnValue(BabyChickenModel.CHICKEN_BABY_TEMPERATE);
		}
	}
}
