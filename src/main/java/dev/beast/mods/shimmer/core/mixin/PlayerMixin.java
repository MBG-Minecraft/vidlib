package dev.beast.mods.shimmer.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.beast.mods.shimmer.core.ShimmerPlayer;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.entity.EntityOverrideValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Player.class)
public abstract class PlayerMixin implements ShimmerPlayer {
	@Shadow
	private Component displayname;

	@Nullable
	@Override
	public Map<EntityOverride<?>, EntityOverrideValue<?>> shimmer$getEntityOverridesMap() {
		return shimmer$sessionData().entityOverridesMap;
	}

	@Override
	public void shimmer$setEntityOverridesMap(@Nullable Map<EntityOverride<?>, EntityOverrideValue<?>> map) {
		shimmer$sessionData().entityOverridesMap = map;
	}

	/**
	 * @author Lat
	 * @reason Optimization
	 */
	@Overwrite
	public void refreshDisplayName() {
		displayname = null;
	}

	@Inject(method = "canHarmPlayer", at = @At("RETURN"), cancellable = true)
	public void shimmer$canHarmPlayer(Player other, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ() && !shimmer$pvp(other)) {
			cir.setReturnValue(false);
		}
	}

	@ModifyReturnValue(method = "getSpeed", at = @At("RETURN"))
	private float shimmer$getSpeed(float original) {
		return original * shimmer$speedMod();
	}

	@ModifyExpressionValue(method = "getFlyingSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Abilities;getFlyingSpeed()F"))
	private float shimmer$getFlyingSpeed(float original) {
		return original * getFlightSpeedMod();
	}
}
