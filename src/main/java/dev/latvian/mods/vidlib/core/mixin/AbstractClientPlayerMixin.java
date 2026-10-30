package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.core.VLClientPlayer;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin implements VLClientPlayer {
	@ModifyReturnValue(method = "getSkin", at = @At("RETURN"))
	private PlayerSkin vl$getSkin(PlayerSkin original) {
		return ClientGameEngine.INSTANCE.overridePlayerSkin((AbstractClientPlayer) (Object) this, original);
	}
}
