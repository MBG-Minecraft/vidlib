package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	/**
	 * 1.21.5 hooked {@code LocalPlayer.drop(boolean)}, which both the drop key and the
	 * creative-inventory drop route flowed through. 26.3 removed that override and moved the
	 * client-side drop into {@code MultiPlayerGameMode.dropItem(LocalPlayer, boolean)}
	 * (called straight from {@code Minecraft.handleKeybinds}), so the VIDL drop override now
	 * lives here. It is still only {@code LocalPlayer}-driven, matching the original.
	 */
	@Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
	private void vl$dropItem(LocalPlayer player, boolean fullStack, CallbackInfo ci) {
		if (PlayerActionHandler.handle(player, PlayerActionType.DROP, true)) {
			ci.cancel();
		}
	}
}
