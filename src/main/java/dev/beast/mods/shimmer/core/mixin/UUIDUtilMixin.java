package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.ShimmerConfig;
import dev.beast.mods.shimmer.util.MiscUtils;
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
	private static void shimmer$createOfflinePlayerUUID(String name, CallbackInfoReturnable<UUID> cir) {
		if (ShimmerConfig.fetchOfflinePlayerData && !FMLLoader.isProduction() && !name.startsWith("Player") && !name.startsWith("Dev")) {
			try {
				Shimmer.LOGGER.info("Fetching offline UUID for " + name + "...");
				var profile = MiscUtils.fetchProfile(name);
				Shimmer.LOGGER.info("UUID for " + name + " found: " + profile.getId());
				cir.setReturnValue(profile.getId());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
