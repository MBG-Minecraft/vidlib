package dev.beast.mods.shimmer.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
	@ModifyExpressionValue(method = {"handleRespawn", "handleLogin"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/CommonPlayerSpawnInfo;isFlat()Z"))
	private boolean shimmer$isFlat(boolean original, @Local CommonPlayerSpawnInfo info) {
		return original || !info.dimensionType().getKey().location().getNamespace().equals("minecraft");
	}
}
