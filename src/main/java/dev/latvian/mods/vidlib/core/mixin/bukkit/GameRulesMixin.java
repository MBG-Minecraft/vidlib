package dev.latvian.mods.vidlib.core.mixin.bukkit;

import dev.latvian.mods.vidlib.core.api.VLGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// Paper/Ignite GameRules#set takes a ServerLevel (the NeoForge variant takes a MinecraftServer).
// The invoker is only resolved at runtime, so this compiles against the NeoForge jar.
@Mixin(GameRules.class)
public abstract class GameRulesMixin implements VLGameRules {
	@Invoker("set")
	public abstract <T> void vl$invokeSet(GameRule<T> rule, T value, ServerLevel level);

	@Override
	public <T> void vl$set(GameRule<T> rule, T value, MinecraftServer server) {
		this.vl$invokeSet(rule, value, server.overworld());
	}
}
