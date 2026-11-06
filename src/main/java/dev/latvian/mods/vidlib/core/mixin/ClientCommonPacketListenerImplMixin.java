package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLClientCommonPacketListener;
import dev.latvian.mods.vidlib.feature.session.LocalClientPlayerSessionData;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonPacketListenerImplMixin implements VLClientCommonPacketListener {
	@Unique
	private LocalClientPlayerSessionData vl$sessionData;

	@Override
	public LocalClientPlayerSessionData vl$sessionData() {
		return vl$sessionData;
	}

	@Override
	public void vl$sessionData(LocalClientPlayerSessionData data) {
		vl$sessionData = data;
	}
}
