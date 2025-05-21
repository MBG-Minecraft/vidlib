package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import net.minecraft.client.renderer.PostChainConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PostChainConfig.FixedSizedTarget.class)
public class PostChainConfigFixedSizeTargetMixin {
	@ModifyExpressionValue(method = "lambda$static$0", at = @At(value = "FIELD", target = "Lnet/minecraft/util/ExtraCodecs;POSITIVE_INT:Lcom/mojang/serialization/Codec;"))
	private static Codec<Integer> vl$negativeWidth(Codec<Integer> original) {
		return Codec.INT;
	}
}
