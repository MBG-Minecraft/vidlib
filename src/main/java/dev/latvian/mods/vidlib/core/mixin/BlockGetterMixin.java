package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.VLLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockGetter.class)
public interface BlockGetterMixin {
	@ModifyReturnValue(method = "clip", at = @At("RETURN"))
	default BlockHitResult vl$clip(BlockHitResult original, @Local(argsOnly = true) ClipContext ctx) {
		return this instanceof VLLevel level ? level.vl$clip(original, ctx) : original;
	}
}
