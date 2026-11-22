package dev.latvian.mods.vidlib.core.mixin.common;

import dev.latvian.mods.klib.util.GameUtils;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
		if (receiving == PacketFlow.SERVERBOUND) {
			if (packetListener instanceof ServerConfigurationPacketListenerImpl lc) {
				VLBridge.capture.apply(GameUtils.getServer(lc), lc.getOwner().id(), packet, Boolean.TRUE);
			} else if (packetListener instanceof ServerGamePacketListenerImpl lc) {
				VLBridge.capture.apply(GameUtils.getServer(lc), lc.player.getUUID(), packet, Boolean.FALSE);
			}
		}
	}
}
