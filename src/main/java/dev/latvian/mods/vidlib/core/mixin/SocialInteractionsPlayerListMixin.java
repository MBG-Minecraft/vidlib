package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.client.gui.screens.social.SocialInteractionsPlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Mixin(SocialInteractionsPlayerList.class)
public class SocialInteractionsPlayerListMixin {
	@Inject(method = "addOnlinePlayers", at = @At("HEAD"), cancellable = true)
	private void vl$addOnlinePlayers(Collection<UUID> ids, Map<UUID, PlayerEntry> playerMap, CallbackInfo ci) {
		if (CommonGameEngine.INSTANCE.privacyMode()) {
			ci.cancel();
		}
	}

	@Inject(method = "updatePlayersFromChatLog", at = @At("HEAD"), cancellable = true)
	private void vl$updatePlayersFromChatLog(Map<UUID, PlayerEntry> playerMap, boolean addPlayers, CallbackInfo ci) {
		if (CommonGameEngine.INSTANCE.privacyMode()) {
			ci.cancel();
		}
	}
}
