package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLServerConfigPacketListener;
import dev.latvian.mods.vidlib.core.VLServerPlayPacketListener;
import dev.latvian.mods.vidlib.feature.session.LoginData;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplMixin implements VLServerConfigPacketListener {
	@Unique
	private final List<LoginData> vl$loginData = new ArrayList<>();

	@Inject(method = "<init>", at = @At("RETURN"))
	private void vl$init(MinecraftServer server, Connection connection, CommonListenerCookie cookie, CallbackInfo ci) {
		vl$sessionData(new ServerSessionData(server, cookie.gameProfile().getId()));
		vl$sessionData().load(server);
	}

	@Override
	public void vl$addLoginData(LoginData data) {
		vl$loginData.add(data);
	}

	@Override
	public void vl$transfer(VLServerPlayPacketListener play, PacketListener packetListener) {
		play.vl$sessionData(vl$sessionData());

		for (var data : vl$loginData) {
			data.transfer(packetListener);
		}
	}

	@Override
	@Invoker("finishCurrentTask")
	public abstract void vl$finishTask(ConfigurationTask.Type type);
}
