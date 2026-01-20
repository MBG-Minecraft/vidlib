package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLClientConfigPacketListener;
import dev.latvian.mods.vidlib.core.VLClientPlayPacketListener;
import dev.latvian.mods.vidlib.feature.session.LocalClientSessionData;
import dev.latvian.mods.vidlib.feature.session.LoginData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public class ClientConfigurationPacketListenerImplMixin implements VLClientConfigPacketListener {
	@Unique
	private final List<LoginData> vl$loginData = new ArrayList<>();

	@Inject(method = "<init>", at = @At("RETURN"))
	private void vl$init(Minecraft mc, Connection connection, CommonListenerCookie cookie, CallbackInfo ci) {
		vl$sessionData(new LocalClientSessionData(mc, cookie.localGameProfile().getId()));
	}

	@Override
	public void vl$addLoginData(LoginData data) {
		vl$loginData.add(data);
	}

	@Override
	public void vl$transfer(VLClientPlayPacketListener play, PacketListener packetListener) {
		play.vl$sessionData(vl$sessionData());

		for (var data : vl$loginData) {
			data.transfer(packetListener);
		}
	}
}
