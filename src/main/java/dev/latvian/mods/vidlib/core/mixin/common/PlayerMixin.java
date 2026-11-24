package dev.latvian.mods.vidlib.core.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.core.api.VLObject;
import dev.latvian.mods.vidlib.feature.entity.VLVehicle;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin implements VLObject {
	@Unique
	private Object vl$sessionData;

	@Override
	public Object vl$get(int id) {
		return id == 522 ? vl$sessionData : null;
	}

	@Override
	public void vl$set(int id, Object value) {
		if (id == 522) {
			vl$sessionData = value;
		}
	}

	@Inject(method = "getName", at = @At("HEAD"), cancellable = true)
	public void vl$getName(CallbackInfoReturnable<Component> cir) {
		var session = PlayerSessionData.of((Player) (Object) this);
		var nickname = session.getNickname();

		if (!Empty.isEmpty(nickname)) {
			cir.setReturnValue(nickname.copy());
		}
	}

	@Inject(method = "canHarmPlayer", at = @At("RETURN"), cancellable = true)
	public void vl$canHarmPlayer(Player other, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ() && !CommonGameEngine.INSTANCE.allowPVP((Player) (Object) this, other)) {
			cir.setReturnValue(false);
		}
	}

	@ModifyReturnValue(method = "getSpeed", at = @At("RETURN"))
	private float vl$getSpeed(float original) {
		return original * CommonGameEngine.INSTANCE.getSpeedModifier((Player) (Object) this);
	}

	@ModifyExpressionValue(method = "getFlyingSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Abilities;getFlyingSpeed()F"))
	private float vl$getFlyingSpeed(float original) {
		return original * CommonGameEngine.INSTANCE.getFlightSpeedModifier((Player) (Object) this);
	}

	@Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
	private void vl$wantsToStopRiding(CallbackInfoReturnable<Boolean> cir) {
		var self = (Player) (Object) this;

		if (self.getVehicle() instanceof VLVehicle v && v.preventDismount(self)) {
			cir.setReturnValue(false);
		}
	}
}
