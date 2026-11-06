package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLServerPacketListener;
import dev.latvian.mods.vidlib.feature.session.ServerPlayerSessionData;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin implements VLServerPacketListener {
	@Unique
	private ServerPlayerSessionData vl$sessionData;

	@Override
	public ServerPlayerSessionData vl$sessionData() {
		return vl$sessionData;
	}

	@Override
	public void vl$sessionData(ServerPlayerSessionData data) {
		vl$sessionData = data;
	}

	@Inject(method = "onDisconnect", at = @At("HEAD"))
	private void vl$close(CallbackInfo ci) {
		if (vl$sessionData != null) {
			vl$sessionData.closed();
		}
	}
}
