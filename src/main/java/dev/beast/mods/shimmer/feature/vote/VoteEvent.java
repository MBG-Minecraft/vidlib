package dev.beast.mods.shimmer.feature.vote;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class VoteEvent extends PlayerEvent implements ICancellableEvent {
	private final CompoundTag data;
	private final boolean yes;

	public VoteEvent(Player player, CompoundTag data, boolean yes) {
		super(player);
		this.data = data;
		this.yes = yes;
	}

	public CompoundTag getData() {
		return data;
	}

	public boolean isYes() {
		return yes;
	}
}
