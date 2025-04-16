package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.net.VidLibPacketPayloadContainer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkRegistry.class)
public class NetworkRegistryMixin {
	@Inject(method = "checkPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/protocol/common/ClientCommonPacketListener;)V", at = @At("HEAD"), cancellable = true)
	private static void vl$checkPacketC2S(Packet<?> packet, ClientCommonPacketListener listener, CallbackInfo ci) {
		if (packet instanceof ServerboundCustomPayloadPacket p && p.payload() instanceof VidLibPacketPayloadContainer) {
			ci.cancel();
		}
	}

	@Inject(method = "checkPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/protocol/common/ServerCommonPacketListener;)V", at = @At("HEAD"), cancellable = true)
	private static void vl$checkPacketS2C(Packet<?> packet, ServerCommonPacketListener listener, CallbackInfo ci) {
		if (packet instanceof ClientboundCustomPayloadPacket p && p.payload() instanceof VidLibPacketPayloadContainer) {
			ci.cancel();
		}
	}
}
