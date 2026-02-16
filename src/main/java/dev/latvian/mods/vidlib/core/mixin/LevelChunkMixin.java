package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
	@Shadow
	@Final
	Level level;

	@Nullable
	@ModifyReturnValue(method = "setBlockState", at = @At("RETURN"))
	private BlockState vl$setBlockState(@Nullable BlockState original, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state) {
		if (original != null) {
			CommonGameEngine.INSTANCE.blockChanged(level, pos, original, state);
		}

		return original;
	}
}
