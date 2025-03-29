package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerDistanceManager;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TickingTracker;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DistanceManager.class)
public abstract class DistanceManagerMixin implements ShimmerDistanceManager {
	@Shadow
	abstract void addTicket(long chunkPos, Ticket<?> p_ticket);

	@Shadow
	abstract void removeTicket(long chunkPos, Ticket<?> ticket);

	@Shadow
	@Final
	private TickingTracker tickingTicketsTracker;

	@Override
	public void shimmer$setLoaded(Ticket<ChunkPos> ticket, boolean loaded) {
		long p = ticket.key.toLong();

		if (loaded) {
			addTicket(p, ticket);
			tickingTicketsTracker.addTicket(p, ticket);
		} else {
			removeTicket(p, ticket);
			tickingTicketsTracker.removeTicket(p, ticket);
		}
	}
}
