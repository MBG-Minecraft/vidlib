package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(ClientSuggestionProvider.class)
public class ClientSuggestionProviderMixin {
	@Inject(method = "getOnlinePlayerNames", at = @At("HEAD"), cancellable = true)
	private void vl$getOnlinePlayerNames(CallbackInfoReturnable<Collection<String>> cir) {
		var override = ClientGameEngine.INSTANCE.overrideCommandOnlinePlayerNames();

		if (override != null) {
			cir.setReturnValue(override);
		}
	}
}
