package dev.latvian.mods.vidlib.core.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import dev.latvian.mods.vidlib.core.api.VLObject;
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
		var name = VLBridge.getPlayerName.apply((Player) (Object) this);

		if (name != null) {
			cir.setReturnValue(name.copy());
		}
	}

	@Inject(method = "canHarmPlayer", at = @At("RETURN"), cancellable = true)
	public void vl$canHarmPlayer(Player other, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ() && !VLBridge.allowPVP.test((Player) (Object) this, other)) {
			cir.setReturnValue(false);
		}
	}

	@ModifyReturnValue(method = "getSpeed", at = @At("RETURN"))
	private float vl$getSpeed(float original) {
		return original * VLBridge.getPlayerSpeedModifier.applyAsFloat((Player) (Object) this);
	}

	@ModifyExpressionValue(method = "getFlyingSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Abilities;getFlyingSpeed()F"))
	private float vl$getFlyingSpeed(float original) {
		return original * VLBridge.getPlayerFlightSpeedModifier.applyAsFloat((Player) (Object) this);
	}

	@Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
	private void vl$wantsToStopRiding(CallbackInfoReturnable<Boolean> cir) {
		var self = (Player) (Object) this;

		if (VLBridge.preventDismount.test(self, self.getVehicle())) {
			cir.setReturnValue(false);
		}
	}
}
