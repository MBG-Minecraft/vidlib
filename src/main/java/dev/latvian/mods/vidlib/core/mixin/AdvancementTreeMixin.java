package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Collection;

@Mixin(AdvancementTree.class)
public class AdvancementTreeMixin {
	@ModifyArg(method = "addAll", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;<init>(Ljava/util/Collection;)V"))
	public Collection<AdvancementHolder> vl$addAll(Collection<AdvancementHolder> original) {
		return CommonGameEngine.INSTANCE.overrideAdvancements(original);
	}
}
