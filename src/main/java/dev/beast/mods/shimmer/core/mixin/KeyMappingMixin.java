package dev.beast.mods.shimmer.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {
	@Shadow
	@Final
	private String name;

	@ModifyExpressionValue(method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants$Type;getOrCreate(I)Lcom/mojang/blaze3d/platform/InputConstants$Key;"))
	private InputConstants.Key shimmer$getKeyCode(InputConstants.Key original, @Local(argsOnly = true) InputConstants.Type type) {
		return switch (name) {
			case "key.saveToolbarActivator", "key.loadToolbarActivator", "key.socialInteractions", "key.advancements" -> type.getOrCreate(InputConstants.UNKNOWN.getValue());
			default -> original;
		};
	}
}
