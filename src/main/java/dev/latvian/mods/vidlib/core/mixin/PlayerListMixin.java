package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpList;
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
public abstract class PlayerListMixin {
	@Shadow
	@Final
	private MinecraftServer server;

	@Redirect(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getDisplayName()Lnet/minecraft/network/chat/Component;"))
	private Component vl$getName(ServerPlayer instance) {
		return Component.literal(instance.getScoreboardName());
	}

	@Inject(method = "placeNewPlayer", at = @At("RETURN"))
	private void vl$placeNewPlayerReturn(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		PlayerSessionData.of(player).respawned(player.level(), true);
	}

	@Inject(method = "respawn", at = @At("RETURN"))
	private void vl$respawn(ServerPlayer player, boolean keepInventory, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir) {
		var newPlayer = cir.getReturnValue();
		PlayerSessionData.of(newPlayer).respawned(newPlayer.level(), false);
	}

	@Inject(method = "remove", at = @At("RETURN"))
	private void vl$remove(ServerPlayer player, CallbackInfo ci) {
		var packetCapture = ServerSessionData.of(server).packetCapture;

		if (packetCapture != null) {
			packetCapture.disconnect(player.getUUID());
		}
	}

	@WrapOperation(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
	private void vl$removeJoinMessage(PlayerList instance, Component message, boolean bypassHiddenChat, Operation<Void> operation) {
		if (!CommonGameEngine.INSTANCE.disableJoinMessages()) {
			operation.call(instance, message, bypassHiddenChat);
		}
	}

	@WrapOperation(method = "op(Lnet/minecraft/server/players/NameAndId;Ljava/util/Optional;Ljava/util/Optional;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/ServerOpList;canBypassPlayerLimit(Lnet/minecraft/server/players/NameAndId;)Z"))
	private boolean vl$canOpBypassPlayerLimit(ServerOpList instance, NameAndId profile, Operation<Boolean> original) {
		return true;
	}
}
