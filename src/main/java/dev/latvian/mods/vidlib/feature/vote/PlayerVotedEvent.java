package dev.latvian.mods.vidlib.feature.vote;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PlayerVotedEvent extends PlayerEvent implements ICancellableEvent {
	private final CompoundTag extraData;
	private final int number;

	public PlayerVotedEvent(Player player, CompoundTag extraData, int number) {
		super(player);
		this.extraData = extraData;
		this.number = number;
	}

	public CompoundTag getExtraData() {
		return extraData;
	}

	public int getNumber() {
		return number;
	}

	public boolean isYes() {
		return number != 0;
	}
}
