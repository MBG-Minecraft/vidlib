package dev.latvian.mods.vidlib.core.mixin.neoforge;

import dev.latvian.mods.vidlib.core.api.VLServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

// 26.3 MinecraftServer#setWeatherParameters takes no level, while Paper/Ignite takes a ServerLevel.
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerWeatherMixin implements VLServer {
	@Override
	public void vl$setWeatherParameters(int rainTime, int thunderTime, boolean raining, boolean thundering) {
		((MinecraftServer) (Object) this).setWeatherParameters(rainTime, thunderTime, raining, thundering);
	}
}
