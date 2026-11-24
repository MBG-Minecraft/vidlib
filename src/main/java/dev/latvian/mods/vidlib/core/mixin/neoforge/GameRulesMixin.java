package dev.latvian.mods.vidlib.core.mixin.neoforge;

import dev.latvian.mods.vidlib.core.api.VLGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// 26.3 GameRules#set takes a MinecraftServer, while Paper/Ignite takes a ServerLevel.
// The unified VLGameRules entry point is implemented per platform so shared code can call it directly.
@Mixin(GameRules.class)
public abstract class GameRulesMixin implements VLGameRules {
	@Invoker("set")
	public abstract <T> void vl$invokeSet(GameRule<T> rule, T value, MinecraftServer server);

	@Override
	public <T> void vl$set(GameRule<T> rule, T value, MinecraftServer server) {
		this.vl$invokeSet(rule, value, server);
	}
}
