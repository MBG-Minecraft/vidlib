package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLIOWorker;
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
public class IOWorkerMixin implements VLIOWorker {
	@Unique
	private ServerLevel vl$level;

	@Shadow
	@Final
	private SequencedMap<ChunkPos, IOWorker.PendingStore> pendingWrites;

	@Override
	public void vl$setLevel(ServerLevel level) {
		vl$level = level;
	}

	@Inject(method = "tellStorePending", at = @At("HEAD"), cancellable = true)
	private void vl$tellStorePending(CallbackInfo ci) {
		if (vl$level != null && vl$level.isImmutableWorld()) {
			pendingWrites.clear();
			ci.cancel();
		}
	}
}
