package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLFoodData;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin implements VLFoodData {
	@Override
	@Accessor("exhaustionLevel")
	public abstract float vl$getExhaustionLevel();

	@Override
	@Accessor("exhaustionLevel")
	public abstract void vl$setExhaustionLevel(float value);

	@Override
	@Accessor("tickTimer")
	public abstract int vl$getTickTimer();

	@Override
	@Accessor("tickTimer")
	public abstract void vl$setTickTimer(int value);

	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	public void vl$tick(ServerPlayer player, CallbackInfo ci) {
		if (CommonGameEngine.INSTANCE.replaceFoodTick(player, (FoodData) (Object) this)) {
			ci.cancel();
		}
	}
}
