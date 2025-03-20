package dev.beast.mods.shimmer.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	/**
	 * @author Lat
	 * @reason Allow editing
	 */
	@Overwrite
	private List<PlayerInfo> getPlayerInfos() {
		return minecraft.player.shimmer$sessionData().getListedPlayers(minecraft);
	}

	@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;getNameForDisplay(Lnet/minecraft/client/multiplayer/PlayerInfo;)Lnet/minecraft/network/chat/Component;"))
	private Component shimmer$modifyPlayerName(Component component, @Local PlayerInfo playerInfo) {
		return minecraft.player.shimmer$sessionData().getClientSessionData(playerInfo.getProfile().getId()).modifyPlayerName(component);
	}
}
