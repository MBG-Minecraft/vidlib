package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLChunkMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin implements VLChunkMap {
	@Shadow
	@Final
	private Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap;

	@Shadow
	protected abstract CompletableFuture<ChunkAccess> scheduleChunkLoad(ChunkPos chunkPos);

	@Override
	public void vl$reloadChunks() {
		for (var pos : LongSets.unmodifiable(updatingChunkMap.keySet())) {
			scheduleChunkLoad(new ChunkPos(pos));
		}
	}
}
