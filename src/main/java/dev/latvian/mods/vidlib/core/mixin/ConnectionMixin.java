package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
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

@Mixin(Connection.class)
public class ConnectionMixin {
	@Shadow
	@Final
	private PacketFlow receiving;

	@Shadow
	@Nullable
	private volatile PacketListener packetListener;

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

	private static void capture(net.minecraft.server.MinecraftServer server, java.util.UUID player, Packet<?> packet, boolean config) {
		if (server == null || VLPlatformHelper.CURRENT.isReplayLevel(server.overworld())) {
			return;
		}

		var packetCapture = server.vl$getPacketCapture(true);

		if (packetCapture != null) {
			packetCapture.getSession(player).capture(packet, config);
		}
	}
}
