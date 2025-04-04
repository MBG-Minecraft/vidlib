package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerIOWorker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SequencedMap;

@Mixin(IOWorker.class)
public class IOWorkerMixin implements ShimmerIOWorker {
	@Unique
	private ServerLevel shimmer$level;

	@Shadow
	@Final
	private SequencedMap<ChunkPos, IOWorker.PendingStore> pendingWrites;

	@Override
	public void shimmer$setLevel(ServerLevel level) {
		shimmer$level = level;
	}

	@Inject(method = "tellStorePending", at = @At("HEAD"), cancellable = true)
	private void shimmer$tellStorePending(CallbackInfo ci) {
		if (shimmer$level != null && shimmer$level.isImmutableWorld()) {
			pendingWrites.clear();
			ci.cancel();
		}
	}
}
