package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import dev.latvian.mods.vidlib.feature.misc.GlobalKeybinds;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {
	@ModifyExpressionValue(method = {
		"<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILnet/minecraft/client/KeyMapping$Category;I)V",
		"<init>(Ljava/lang/String;Lnet/neoforged/neoforge/client/settings/IKeyConflictContext;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILnet/minecraft/client/KeyMapping$Category;)V",
		"<init>(Ljava/lang/String;Lnet/neoforged/neoforge/client/settings/IKeyConflictContext;Lnet/neoforged/neoforge/client/settings/KeyModifier;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILnet/minecraft/client/KeyMapping$Category;)V",
	}, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants$Type;getOrCreate(I)Lcom/mojang/blaze3d/platform/InputConstants$Key;"))
	private static InputConstants.Key vl$getKeyCode(InputConstants.Key original, @Local(argsOnly = true) String name) {
		return GlobalKeybinds.modifyDefaultKeys(name, original);
	}
}
