package dev.beast.mods.shimmer.feature.npc;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface NPCValueGetter<T> {
	T get(Player player, float delta);
}
