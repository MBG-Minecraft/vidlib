package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
	/**
	 * 1.21.5 hooked {@code prepare(...)}, which built the {@code RecipeMap} from a plain
	 * {@code Iterable<RecipeHolder<?>>} - so this extension point could filter or augment the
	 * recipe list before the map existed.
	 * <p>
	 * 26.3 made recipe loading registry-driven: {@code RecipeMap} is now built inside
	 * {@code RecipeMap.create(HolderLookup)} from {@code Registries.RECIPE}, so there is no
	 * intermediate iterable to modify at build time any more. The equivalent point where the
	 * manager still enumerates the complete recipe set during loading is
	 * {@link RecipeManager#finalizeRecipeLoading}, which reads it via
	 * {@link RecipeMap#values()}. The signature there matches the extension point exactly, so
	 * the hook (and its {@code CommonGameEngine} contract) is preserved unchanged.
	 */
	@ModifyExpressionValue(
		method = "finalizeRecipeLoading",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeMap;values()Ljava/util/Collection;")
	)
	private Collection<RecipeHolder<?>> vl$modifyRecipeList(Collection<RecipeHolder<?>> original) {
		if (original.isEmpty()) {
			return original;
		}

		Iterable<RecipeHolder<?>> modified = CommonGameEngine.INSTANCE.modifyRecipeList(original);

		if (modified == original) {
			return original;
		}

		List<RecipeHolder<?>> list = new ArrayList<>();
		modified.forEach(list::add);
		return list;
	}
}
