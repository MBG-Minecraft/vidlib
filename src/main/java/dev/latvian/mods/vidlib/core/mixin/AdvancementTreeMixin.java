package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(AdvancementTree.class)
public class AdvancementTreeMixin {
	@Inject(method = "addAll", at = @At("HEAD"), cancellable = true)
	public void vl$addAll(Collection<AdvancementHolder> advancements, CallbackInfo ci) {
		if (CommonGameEngine.INSTANCE.disableAdvancements()) {
			ci.cancel();
		}
	}
}
