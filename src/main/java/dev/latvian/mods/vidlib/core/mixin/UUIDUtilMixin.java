package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.UUIDUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(UUIDUtil.class)
public class UUIDUtilMixin {
	@Inject(method = "createOfflinePlayerUUID", at = @At("HEAD"), cancellable = true)
	private static void vl$createOfflinePlayerUUID(String name, CallbackInfoReturnable<UUID> cir) {
		var uuid = CommonGameEngine.INSTANCE.createOfflinePlayerUUID(name);

		if (uuid != null) {
			cir.setReturnValue(uuid);
		}
	}
}
