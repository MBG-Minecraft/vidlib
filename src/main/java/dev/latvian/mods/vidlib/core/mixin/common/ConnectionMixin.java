package dev.latvian.mods.vidlib.core.mixin.common;

import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(Connection.class)
public class ConnectionMixin {
	@Shadow
	@Final
	private PacketFlow receiving;

	@Shadow
	@Nullable
	private volatile PacketListener packetListener;

	@Shadow
	private volatile boolean sendLoginDisconnect;

	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"))
	private void vl$send(Packet<?> packet, io.netty.channel.ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
		if (receiving == PacketFlow.SERVERBOUND) {
			if (packetListener instanceof ServerConfigurationPacketListenerImpl lc) {
				capture(ServerLifecycleHooks.getCurrentServer(), lc.getOwner().id(), packet, true);
			} else if (packetListener instanceof ServerGamePacketListenerImpl lc) {
				capture(ServerLifecycleHooks.getCurrentServer(), lc.player.getUUID(), packet, false);
			}
		}
	}

	private static void capture(MinecraftServer server, UUID player, Packet<?> packet, boolean config) {
		if (server == null) {
			return;
		}

		var session = ServerSessionData.of(server);

		if (session.isReplay) {
			return;
		}

		var packetCapture = session.startPacketCapture();

		if (packetCapture != null) {
			packetCapture.getSession(player).capture(packet, config);
		}
	}
}
