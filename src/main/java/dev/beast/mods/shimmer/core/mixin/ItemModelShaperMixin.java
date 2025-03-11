package dev.beast.mods.shimmer.core.mixin;

import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemModelShaper.class)
public class ItemModelShaperMixin {
	@Inject(method = "getItemModel(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/BakedModel;", at = @At("HEAD"), cancellable = true)
	public void shimmer$getItemModel(ItemStack stack, CallbackInfoReturnable<BakedModel> cir) {
		var customData = stack.get(DataComponents.CUSTOM_DATA);

		if (customData != null && customData.getUnsafe().contains("shimmer:model")) {
			var model = ((ItemModelShaper) (Object) this).getModelManager().getModel(new ModelResourceLocation(ResourceLocation.parse(customData.getUnsafe().getString("shimmer:model")), "inventory"));

			if (model != null) {
				cir.setReturnValue(model);
			}
		}
	}
}
