package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLMinecraftServer;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements VLMinecraftServer {
	@Unique
	private ServerSessionData vl$globalData;

	@Override
	@Nullable
	public ServerSessionData vl$getGlobalData() {
		if (vl$globalData == null) {
			vl$globalData = new ServerSessionData((MinecraftServer) (Object) this);
			vl$globalData.load();
		}

		return vl$globalData;
	}
}
