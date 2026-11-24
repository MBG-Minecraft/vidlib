package dev.latvian.mods.vidlib.core.mixin.bukkit;

import dev.latvian.mods.vidlib.core.api.VLServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// Paper/Ignite MinecraftServer#setWeatherParameters takes a ServerLevel (the NeoForge variant does not).
// The invoker is only resolved at runtime, so this compiles against the NeoForge jar.
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerWeatherMixin implements VLServer {
	@Invoker("setWeatherParameters")
	public abstract void vl$invokeSetWeatherParameters(ServerLevel level, int rainTime, int thunderTime, boolean raining, boolean thundering);

	@Override
	public void vl$setWeatherParameters(int rainTime, int thunderTime, boolean raining, boolean thundering) {
		this.vl$invokeSetWeatherParameters(((MinecraftServer) (Object) this).overworld(), rainTime, thunderTime, raining, thundering);
	}
}
