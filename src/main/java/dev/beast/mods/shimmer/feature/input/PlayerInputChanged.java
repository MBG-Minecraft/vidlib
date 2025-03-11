package dev.beast.mods.shimmer.feature.input;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PlayerInputChanged extends PlayerEvent {
	private final PlayerInput prevInput;
	private final PlayerInput input;

	public PlayerInputChanged(Player player, PlayerInput prevInput, PlayerInput input) {
		super(player);
		this.prevInput = prevInput;
		this.input = input;
	}

	public PlayerInput getPrevInput() {
		return prevInput;
	}

	public PlayerInput getInput() {
		return input;
	}
}
