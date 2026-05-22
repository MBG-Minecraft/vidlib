package dev.latvian.mods.vidlib.core.mixin.neo;

import dev.latvian.mods.vidlib.core.VLTicketOwner;
import dev.latvian.mods.vidlib.core.VLTicketTracker;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(ForcedChunkManager.TicketTracker.class)
public abstract class TicketTrackerMixin<T extends Comparable<? super T>> implements VLTicketTracker<T> {
	@Shadow
	@Final
	private Long2ObjectMap<Set<VLTicketOwner<T>>> sourcesLoading;

	@Override
	public Long2ObjectMap<Set<VLTicketOwner<T>>> vl$getTickets() {
		return sourcesLoading;
	}
}
