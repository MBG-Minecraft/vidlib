package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.vidlib.core.VLServerPlayPacketListener;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements VLServerPlayPacketListener {
	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	private void detectRateSpam() {
	}

	@WrapOperation(method = "removePlayerFromWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
	private void vl$removeJoinMessage(PlayerList instance, Component message, boolean bypassHiddenChat, Operation<Void> operation) {
		if (!CommonGameEngine.INSTANCE.disableJoinMessages()) {
			operation.call(instance, message, bypassHiddenChat);
		}
	}
}
