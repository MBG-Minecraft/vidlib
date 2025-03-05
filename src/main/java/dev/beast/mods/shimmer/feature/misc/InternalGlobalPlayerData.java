package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.feature.session.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class InternalGlobalPlayerData extends PlayerData {
	public Optional<Component> displayName;
	public ItemStack hat;

	InternalGlobalPlayerData() {
		super(InternalPlayerData.GLOBAL);
		this.displayName = Optional.empty();
		this.hat = ItemStack.EMPTY;
	}

	InternalGlobalPlayerData(
		Optional<Component> displayName,
		ItemStack hat
	) {
		super(InternalPlayerData.GLOBAL);
		this.displayName = displayName;
		this.hat = hat;
	}
}
