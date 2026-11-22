package dev.latvian.mods.vidlib.core.mixin.bukkit;

import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Inject(method = "remove(Lnet/minecraft/server/level/ServerPlayer;Lnet/kyori/adventure/text/Component;)Lnet/kyori/adventure/text/Component;", at = @At("RETURN"))
	private void vl$remove(CallbackInfoReturnable<?> cir, @Local(argsOnly = true) ServerPlayer player) {
		VLBridge.playerRemoved.accept(player.level().getServer(), player);
	}
}
