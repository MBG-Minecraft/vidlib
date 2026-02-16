package dev.latvian.mods.vidlib.core.mixin.mod;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.maxhenkel.voicechat.voice.client.PTTKeyHandler;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(PTTKeyHandler.class)
public class PTTKeyHandlerMixin {
	@ModifyReturnValue(method = {"isPTTDown", "isAnyDown"}, at = @At("RETURN"))
	private boolean vl$isPTT(boolean original) {
		if (!original && ClientGameEngine.INSTANCE.isVoiceChatPTTDown()) {
			return true;
		}

		return original;
	}
}
