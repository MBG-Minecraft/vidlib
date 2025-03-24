package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SectionStorage.class)
public class SectionStorageMixin {
	@Shadow
	@Final
	protected LevelHeightAccessor levelHeightAccessor;

	@Inject(method = "writeChunk(Lnet/minecraft/world/level/ChunkPos;)V", at = @At("HEAD"), cancellable = true)
	private void shimmer$writeChunk(ChunkPos pos, CallbackInfo ci) {
		if (levelHeightAccessor instanceof ShimmerServerLevel level && level.shimmer$cancelWrite()) {
			ci.cancel();
		}
	}
}
