package dev.latvian.mods.vidlib.core.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;

public interface VLGameRules {
	<T> void vl$set(GameRule<T> rule, T value, MinecraftServer server);
}
