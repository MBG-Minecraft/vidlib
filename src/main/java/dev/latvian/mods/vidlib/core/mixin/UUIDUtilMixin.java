package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibConfig;
import dev.latvian.mods.vidlib.util.MiscUtils;
import net.minecraft.core.UUIDUtil;
import net.neoforged.fml.loading.FMLLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(UUIDUtil.class)
public class UUIDUtilMixin {
	@Inject(method = "createOfflinePlayerUUID", at = @At("HEAD"), cancellable = true)
	private static void vl$createOfflinePlayerUUID(String name, CallbackInfoReturnable<UUID> cir) {
		if (VidLibConfig.fetchOfflinePlayerData && !FMLLoader.isProduction() && !name.startsWith("Player") && !name.startsWith("Dev")) {
			try {
				VidLib.LOGGER.info("Fetching offline UUID for " + name + "...");
				var profile = MiscUtils.fetchProfile(name);
				VidLib.LOGGER.info("UUID for " + name + " found: " + profile.getId());
				cir.setReturnValue(profile.getId());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
