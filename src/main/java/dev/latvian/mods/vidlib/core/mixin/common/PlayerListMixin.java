package dev.latvian.mods.vidlib.core.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	@WrapOperation(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getDisplayName()Lnet/minecraft/network/chat/Component;"))
	private Component vl$getName(ServerPlayer instance, Operation<Component> original) {
		return Component.literal(instance.getScoreboardName());
	}
}
