package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLClientCommonPacketListener;
import dev.latvian.mods.vidlib.feature.session.ClientWorldSessionData;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonPacketListenerImplMixin implements VLClientCommonPacketListener {
	@Unique
	private ClientWorldSessionData vl$worldData;

	@Override
	public ClientWorldSessionData vl$getWorldData() {
		return vl$worldData;
	}

	@Override
	public void vl$setWorldData(ClientWorldSessionData data) {
		vl$worldData = data;
	}
}
