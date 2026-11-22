package dev.latvian.mods.vidlib.core.mixin.common;

import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
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
		VLBridge.playerRespawned.apply(player, player.level(), true);
	}

	@Inject(method = "respawn", at = @At("RETURN"))
	private void vl$respawn(ServerPlayer player, boolean keepInventory, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir) {
		var newPlayer = cir.getReturnValue();
		VLBridge.playerRespawned.apply(newPlayer, newPlayer.level(), false);
	}

	@Inject(method = "remove", at = @At("RETURN"))
	private void vl$remove(ServerPlayer player, CallbackInfo ci) {
		VLBridge.playerRemoved.accept(server, player);
	}
}
