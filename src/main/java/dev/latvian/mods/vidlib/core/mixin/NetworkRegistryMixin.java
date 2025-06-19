package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.net.VidLibPacketPayloadContainer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.neoforged.neoforge.network.negotiation.NegotiableNetworkComponent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;

@Mixin(NetworkRegistry.class)
public class NetworkRegistryMixin {
	@Inject(method = "checkPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/protocol/common/ClientCommonPacketListener;)V", at = @At("HEAD"), cancellable = true)
	private static void vl$checkPacketC2S(Packet<?> packet, ClientCommonPacketListener listener, CallbackInfo ci) {
		if (packet instanceof ServerboundCustomPayloadPacket(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) && payload instanceof VidLibPacketPayloadContainer) {
			ci.cancel();
		}
	}

	@Inject(method = "checkPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/protocol/common/ServerCommonPacketListener;)V", at = @At("HEAD"), cancellable = true)
	private static void vl$checkPacketS2C(Packet<?> packet, ServerCommonPacketListener listener, CallbackInfo ci) {
		if (packet instanceof ClientboundCustomPayloadPacket(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) && payload instanceof VidLibPacketPayloadContainer) {
			ci.cancel();
		}
	}

	@ModifyArgs(
		method = "initializeNeoForgeConnection(Lnet/minecraft/network/protocol/configuration/ServerConfigurationPacketListener;Ljava/util/Map;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/neoforged/neoforge/network/negotiation/NetworkComponentNegotiator;" +
				"negotiate(Ljava/util/List;Ljava/util/List;)" +
				"Lnet/neoforged/neoforge/network/negotiation/NegotiationResult;"
		)
	)
	private static void vl$modifyArgsForModded(Args args) {
		List<NegotiableNetworkComponent> serverComponents = args.get(0);
		List<NegotiableNetworkComponent> clientComponents = args.get(1);
		var modifiedServer = new ArrayList<>(serverComponents);
		var modifiedClient = new ArrayList<>(clientComponents);
		modifiedServer.addAll(clientComponents);
		args.set(0, modifiedServer);
		args.set(1, modifiedClient);
	}

	@ModifyArgs(
		method = "initializeOtherConnection(Lnet/minecraft/network/protocol/configuration/ServerConfigurationPacketListener;)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/neoforged/neoforge/network/negotiation/NetworkComponentNegotiator;" +
				"negotiate(Ljava/util/List;Ljava/util/List;)" +
				"Lnet/neoforged/neoforge/network/negotiation/NegotiationResult;"
		)
	)
	private static void vl$modifyArgsForVanilla(Args args) {
		List<NegotiableNetworkComponent> serverComponents = args.get(0);
		List<NegotiableNetworkComponent> clientComponents = args.get(1);
		var modifiedServer = new ArrayList<>(serverComponents);
		var modifiedClient = new ArrayList<>(clientComponents);
		modifiedServer.addAll(clientComponents);
		args.set(0, modifiedServer);
		args.set(1, modifiedClient);
	}
}
