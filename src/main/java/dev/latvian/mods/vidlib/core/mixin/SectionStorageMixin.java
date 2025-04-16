package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLServerLevel;
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
	private void vl$writeChunk(ChunkPos pos, CallbackInfo ci) {
		if (levelHeightAccessor instanceof VLServerLevel level && level.isImmutableWorld()) {
			ci.cancel();
		}
	}
}
