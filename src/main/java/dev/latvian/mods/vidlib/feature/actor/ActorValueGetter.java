package dev.latvian.mods.vidlib.feature.actor;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface ActorValueGetter<T> {
	T get(Player player, float delta);
}
