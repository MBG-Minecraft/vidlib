package dev.latvian.mods.vidlib.core.mixin;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import dev.latvian.mods.vidlib.core.VLServerPacketListener;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
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
	private void vl$send(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
		if (receiving == PacketFlow.SERVERBOUND && packetListener instanceof ServerCommonPacketListenerImpl l && packetListener instanceof VLServerPacketListener vl && !vl.vl$server().overworld().isReplayLevel()) {
			if (l instanceof ServerConfigurationPacketListenerImpl lc) {
				var packetCapture = vl.vl$server().vl$getPacketCapture(true);

				if (packetCapture != null) {
					packetCapture.getSession(lc.getOwner().id()).capture(packet, true);
				}
			} else if (l instanceof ServerGamePacketListenerImpl lc) {
				var packetCapture = vl.vl$server().vl$getPacketCapture(true);

				if (packetCapture != null) {
					packetCapture.getSession(lc.getOwner().id()).capture(packet, false);
				}
			}
		}
	}
}
