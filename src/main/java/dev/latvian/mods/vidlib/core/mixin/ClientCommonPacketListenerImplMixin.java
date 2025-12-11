package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLClientCommonPacketListener;
import dev.latvian.mods.vidlib.feature.session.LocalClientSessionData;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonPacketListenerImplMixin implements VLClientCommonPacketListener {
	@Unique
	private LocalClientSessionData vl$sessionData;

	@Override
	public LocalClientSessionData vl$sessionData() {
		return vl$sessionData;
	}

	@Override
	public void vl$sessionData(LocalClientSessionData data) {
		vl$sessionData = data;
	}
}
