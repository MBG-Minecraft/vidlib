package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.core.VLClientPlayer;
import dev.latvian.mods.vidlib.feature.cape.VLCape;
import dev.latvian.mods.vidlib.feature.skin.VLSkin;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin implements VLClientPlayer {

	@Shadow
	@Nullable
	protected abstract PlayerInfo getPlayerInfo();

	@Override
	@Nullable
	public GameType getGameMode() {
		var info = getPlayerInfo();
		return info == null ? null : info.getGameMode();
	}

	@Inject(
		method = "getSkin",
		at = @At("HEAD"),
		cancellable = true
	)
	private void vidlib$overrideSkin(CallbackInfoReturnable<PlayerSkin> cir) {
		var skinOverride = VLSkin.getSkinOverride((AbstractClientPlayer) (Object) this);
		skinOverride.ifPresent(cir::setReturnValue);
	}

	@ModifyReturnValue(
		method = "getSkin",
		at = @At("RETURN")
	)
	private PlayerSkin vidlib$addCape(PlayerSkin oldSkin) {
		return VLCape.skinWithCape((AbstractClientPlayer) (Object) this, oldSkin);
	}

}
