package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLServerPacketListener;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin implements VLServerPacketListener {
	@Unique
	private ServerSessionData vl$sessionData;

	@Override
	public ServerSessionData vl$sessionData() {
		return vl$sessionData;
	}

	@Override
	public void vl$sessionData(ServerSessionData data) {
		vl$sessionData = data;
	}

	@Inject(method = "onDisconnect", at = @At("RETURN"))
	private void vl$close(CallbackInfo ci) {
		if (vl$sessionData != null) {
			vl$sessionData.closed();
		}
	}

	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	private void detectRateSpam() {
	}
}
