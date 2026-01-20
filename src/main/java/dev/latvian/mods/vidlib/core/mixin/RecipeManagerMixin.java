package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Collection;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
	@ModifyArg(
		method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeMap;create(Ljava/lang/Iterable;)Lnet/minecraft/world/item/crafting/RecipeMap;"),
		index = 0
	)
	private Iterable<RecipeHolder<?>> vl$modifyRecipeList(Iterable<RecipeHolder<?>> original) {
		if (original instanceof Collection<RecipeHolder<?>> c && c.isEmpty()) {
			return original;
		}

		return CommonGameEngine.INSTANCE.modifyRecipeList(original);
	}
}
