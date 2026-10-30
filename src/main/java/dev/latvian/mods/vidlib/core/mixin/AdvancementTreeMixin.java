package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 26.3 builds the working list with {@code Lists.newArrayList(advancements)} instead of
 * {@code new ArrayList<>(advancements)}, so the argument override now wraps that call.
 */
@Mixin(AdvancementTree.class)
public class AdvancementTreeMixin {
	@ModifyExpressionValue(method = "addAll", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList(Ljava/lang/Iterable;)Ljava/util/ArrayList;"))
	private ArrayList<AdvancementHolder> vl$addAll(ArrayList<AdvancementHolder> original) {
		Collection<AdvancementHolder> override = CommonGameEngine.INSTANCE.overrideAdvancements(original);
		return override instanceof ArrayList<AdvancementHolder> list ? list : new java.util.ArrayList<>(override);
	}
}
