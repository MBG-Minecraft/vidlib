package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerChunkMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin implements ShimmerChunkMap {
	@Shadow
	@Final
	ServerLevel level;

	@Shadow
	@Final
	private Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap;

	@Shadow
	protected abstract CompletableFuture<ChunkAccess> scheduleChunkLoad(ChunkPos chunkPos);

	//@Unique
	//private static List<AAIBB> shimmer$anchored = null;

	@Override
	public void shimmer$reloadChunks() {
		for (var pos : LongSets.unmodifiable(updatingChunkMap.keySet())) {
			scheduleChunkLoad(new ChunkPos(pos));
		}
	}

	/*
	@Inject(method = "dropChunk", at = @At("HEAD"), cancellable = true)
	private static void shimmer$dropChunk(ServerPlayer player, ChunkPos chunkPos, CallbackInfo ci) {
		if (shimmer$anchored != null) {
			for (var aaibb : shimmer$anchored) {
				if (aaibb.containsChunk(chunkPos)) {
					ci.cancel();
					return;
				}
			}
		}
	}

	@Inject(method = "applyChunkTrackingView", at = @At("HEAD"))
	private void shimmer$applyChunkTrackingViewPre(ServerPlayer player, ChunkTrackingView chunkTrackingView, CallbackInfo ci) {
		shimmer$anchored = level.getServerData().get(InternalServerData.ANCHOR).shapes().get(level.dimension());
	}

	@Inject(method = "applyChunkTrackingView", at = @At("RETURN"))
	private void shimmer$applyChunkTrackingViewPost(ServerPlayer player, ChunkTrackingView chunkTrackingView, CallbackInfo ci) {
		shimmer$anchored = null;
	}

	@ModifyConstant(method = "setServerViewDistance", constant = @Constant(intValue = 32))
	private int shimmer$setServerViewDistance(int original) {
		return ShimmerConfig.maxChunkDistance;
	}
	 */
}
