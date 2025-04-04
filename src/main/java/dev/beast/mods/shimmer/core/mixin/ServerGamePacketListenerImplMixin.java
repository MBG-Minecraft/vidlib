package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerServerPacketListener;
import dev.beast.mods.shimmer.feature.session.ShimmerServerSessionData;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin implements ShimmerServerPacketListener {
	@Unique
	private ShimmerServerSessionData shimmer$sessionData;

	@Override
	public ShimmerServerSessionData shimmer$sessionData() {
		return shimmer$sessionData;
	}

	@Override
	public void shimmer$sessionData(ShimmerServerSessionData data) {
		shimmer$sessionData = data;
	}

	@Inject(method = "onDisconnect", at = @At("RETURN"))
	private void shimmer$close(CallbackInfo ci) {
		if (shimmer$sessionData != null) {
			shimmer$sessionData.closed();
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
