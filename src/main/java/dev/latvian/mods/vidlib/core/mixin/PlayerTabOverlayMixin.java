package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
		return minecraft.player.vl$sessionData().getListedPlayers();
	}

	@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;getNameForDisplay(Lnet/minecraft/client/multiplayer/PlayerInfo;)Lnet/minecraft/network/chat/Component;"))
	private Component vl$modifyPlayerName(Component component, @Local PlayerInfo playerInfo) {
		return ClientGameEngine.INSTANCE.getPlayerListName(minecraft, playerInfo, component);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"))
	private int vl$renderName(GuiGraphics instance, Font font, Component text, int x, int y, int color, @Local PlayerInfo playerInfo) {
		return instance.drawString(font, text, x, y, color, false);
	}
}
