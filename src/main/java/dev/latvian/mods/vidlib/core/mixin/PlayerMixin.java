package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.core.VLPlayer;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.damagesource.IScalingFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin implements VLPlayer {
	@Shadow
	private Component displayname;

	/**
	 * @author Lat
	 * @reason Optimization
	 */
	@Overwrite
	public void refreshDisplayName() {
		displayname = null;
		var s = vl$sessionData();

		if (s != null) {
			s.refreshListedPlayers();
		}
	}

	@Inject(method = "getName", at = @At("HEAD"), cancellable = true)
	public void vl$getName(CallbackInfoReturnable<Component> cir) {
		var nickname = vl$sessionData() == null ? null : getOptional(InternalPlayerData.NICKNAME);

		if (!Empty.isEmpty(nickname)) {
			cir.setReturnValue(nickname.copy());
		}
	}

	@Inject(method = "canHarmPlayer", at = @At("RETURN"), cancellable = true)
	public void vl$canHarmPlayer(Player other, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ() && !CommonGameEngine.INSTANCE.allowPVP(vl$self(), other)) {
			cir.setReturnValue(false);
		}
	}

	@ModifyReturnValue(method = "getSpeed", at = @At("RETURN"))
	private float vl$getSpeed(float original) {
		return original * CommonGameEngine.INSTANCE.getSpeedModifier(vl$self());
	}

	@ModifyExpressionValue(method = "getFlyingSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Abilities;getFlyingSpeed()F"))
	private float vl$getFlyingSpeed(float original) {
		return original * CommonGameEngine.INSTANCE.getFlightSpeedModifier(vl$self());
	}

	@WrapOperation(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/damagesource/IScalingFunction;scaleDamage(Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/entity/player/Player;FLnet/minecraft/world/Difficulty;)F"))
	private float vl$scaleDamage(IScalingFunction scalingFunction, DamageSource source, Player player, float damage, Difficulty difficulty, Operation<Float> operation) {
		return CommonGameEngine.INSTANCE.getScaleDamageWithDifficulty((ServerPlayer) vl$self()) ? operation.call(scalingFunction, source, player, damage, difficulty) : damage;
	}

	@Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
	private void vl$wantsToStopRiding(CallbackInfoReturnable<Boolean> cir) {
		var self = (Player) (Object) this;
		var v = self.getVehicle();

		if (v != null && v.preventDismount(self)) {
			cir.setReturnValue(false);
		}
	}
}
