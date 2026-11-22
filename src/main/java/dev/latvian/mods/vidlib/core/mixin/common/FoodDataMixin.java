package dev.latvian.mods.vidlib.core.mixin.common;

import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	public void vl$tick(ServerPlayer player, CallbackInfo ci) {
		if (VLBridge.replaceFoodTick.test(player, (FoodData) (Object) this)) {
			ci.cancel();
		}
	}
}
