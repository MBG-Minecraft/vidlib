package dev.beast.mods.shimmer.core.mixin;

import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(ItemModelShaper.class)
public class ItemModelShaperMixin {
	@Unique
	private final Map<String, BakedModel> shimmer$cache = new HashMap<>();

	@Inject(method = "getItemModel(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/BakedModel;", at = @At("HEAD"), cancellable = true)
	public void shimmer$getItemModel(ItemStack stack, CallbackInfoReturnable<BakedModel> cir) {
		var customData = stack.get(DataComponents.CUSTOM_DATA);

		if (customData != null && customData.getUnsafe().contains("shimmer:model")) {
			var string = customData.getUnsafe().getString("shimmer:model");
			var model = shimmer$cache.get(string);
			var manager = ((ItemModelShaper) (Object) this).getModelManager();

			if (model == null) {
				var str = string.split("#", 2);
				var modelLocation = new ModelResourceLocation(ResourceLocation.parse(str[0]), str.length == 1 ? "inventory" : str[1]);
				model = manager.getModel(modelLocation);

				if (model == null) {
					model = manager.getMissingModel();
				}

				shimmer$cache.put(string, model);
			}

			if (model != manager.getMissingModel()) {
				cir.setReturnValue(model);
			}
		}
	}

	@Inject(method = "rebuildCache", at = @At("RETURN"))
	public void shimmer$rebuildCache(CallbackInfo ci) {
		shimmer$cache.clear();
	}
}
