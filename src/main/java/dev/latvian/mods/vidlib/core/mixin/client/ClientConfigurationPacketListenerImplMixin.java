package dev.latvian.mods.vidlib.core.mixin.client;

import dev.latvian.mods.vidlib.core.api.client.VLClientConfigPacketListener;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public abstract class ClientConfigurationPacketListenerImplMixin implements VLClientConfigPacketListener {
	@Unique
	private ClientSessionData vl$globalData;

	@Inject(method = "<init>", at = @At("RETURN"))
	protected void vl$init(Minecraft minecraft, Connection connection, CommonListenerCookie cookie, CallbackInfo ci) {
		vl$globalData = new ClientSessionData(minecraft, cookie.receivedRegistries(), cookie.localGameProfile().id());
	}

	@Override
	public ClientSessionData vl$getGlobalData() {
		return vl$globalData;
	}
}
