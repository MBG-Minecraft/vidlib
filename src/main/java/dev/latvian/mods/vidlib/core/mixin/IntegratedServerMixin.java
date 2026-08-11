package dev.latvian.mods.vidlib.core.mixin;

import dev.mrbeastgaming.mods.hub.api.gateway.HubServerGateway;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
	@Inject(method = "publishServer", at = @At("RETURN"))
	private void vl$publishServer(GameType gameMode, boolean cheats, int port, CallbackInfoReturnable<Boolean> cir) {
		var gateway = HubServerGateway.instance;

		if (gateway != null) {
			HubServerGateway.updateInfo((IntegratedServer) (Object) this, gateway);
		}
	}
}
