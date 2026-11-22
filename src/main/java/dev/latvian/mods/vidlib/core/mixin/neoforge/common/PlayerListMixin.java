package dev.latvian.mods.vidlib.core.mixin.neoforge.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
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
