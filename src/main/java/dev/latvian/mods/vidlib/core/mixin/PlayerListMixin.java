package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.VLServerConfigPacketListener;
import dev.latvian.mods.vidlib.core.VLServerPlayPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Shadow
	@Final
	private MinecraftServer server;

	@Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"))
	private void vl$placeNewPlayerHead(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci, @Local ServerGamePacketListenerImpl packetListener) {
		if (connection.getPacketListener() instanceof VLServerConfigPacketListener config && packetListener instanceof VLServerPlayPacketListener play) {
			config.vl$transfer(play, packetListener);
		}
	}

	@Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getName()Lnet/minecraft/network/chat/Component;"))
	private Component vl$getName(ServerPlayer instance) {
		return Component.literal(instance.getScoreboardName());
	}

	@Inject(method = "placeNewPlayer", at = @At("RETURN"))
	private void vl$placeNewPlayerReturn(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		player.vl$sessionData().respawned(player.level(), true);
	}

	@Inject(method = "respawn", at = @At("RETURN"))
	private void vl$respawn(ServerPlayer player, boolean keepInventory, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir) {
		var newPlayer = cir.getReturnValue();
		newPlayer.vl$sessionData().respawned(newPlayer.level(), false);
	}

	@Inject(method = "remove", at = @At("RETURN"))
	private void vl$remove(ServerPlayer player, CallbackInfo ci) {
		var packetCapture = server.vl$getPacketCapture(false);

		if (packetCapture != null) {
			packetCapture.disconnect(player.getUUID());
		}
	}
}
