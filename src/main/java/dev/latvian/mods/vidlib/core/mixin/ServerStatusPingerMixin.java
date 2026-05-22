package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.status.ServerStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.multiplayer.ServerStatusPinger$1")
public class ServerStatusPingerMixin {
	@Inject(method = "lambda$handleStatusResponse$2", at = @At("HEAD"), cancellable = true)
	private static void vl$hideServerPingPlayers(ServerData serverData, ServerStatus.Players players, CallbackInfo ci) {
		if (ClientGameEngine.INSTANCE.overrideServerPingPlayers(serverData, players)) {
			ci.cancel();
		}
	}
}
