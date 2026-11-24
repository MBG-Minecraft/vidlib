package dev.latvian.mods.vidlib.core.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import dev.latvian.mods.vidlib.core.api.VLObject;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements VLObject {
	@Shadow
	private EnvironmentAttributeSystem environmentAttributes;

	@Unique
	private Object vl$sessionData;

	@Unique
	private EnvironmentAttributeSystem vl$environmentAttributes;

	@Override
	public Object vl$get(int id) {
		return switch (id) {
			case 0 -> environmentAttributes;
			case 522 -> vl$sessionData;
			default -> null;
		};
	}

	@Override
	public void vl$set(int id, Object value) {
		switch (id) {
			case 522 -> vl$sessionData = value;
			case 523 -> vl$environmentAttributes = (EnvironmentAttributeSystem) value;
		}
	}

	@Inject(method = "announceSleepStatus", at = @At("HEAD"), cancellable = true)
	private void vl$announceSleepStatus(CallbackInfo ci) {
		if (VLBridge.disableSleepStatusAnnouncement.test((ServerLevel) (Object) this)) {
			ci.cancel();
		}
	}

	@Inject(method = "tickPrecipitation", at = @At("RETURN"))
	private void vl$tickPrecipitation(BlockPos blockPos, CallbackInfo ci, @Local(ordinal = 1) BlockPos motionBlockedPos, @Local(ordinal = 2) BlockPos belowMotionBlockedPos) {
		VLBridge.tickPrecipitation.apply((ServerLevel) (Object) this, blockPos, motionBlockedPos, belowMotionBlockedPos);
	}

	@ModifyConstant(method = {"runBlockEvents", "levelEvent"}, constant = @Constant(doubleValue = 64D))
	private double vl$eventDistance(double constant) {
		return Math.max(constant, 512D);
	}

	@ModifyExpressionValue(method = {"environmentAttributes()Lnet/minecraft/world/attribute/EnvironmentAttributeSystem;", "getMoonBrightness"}, at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerLevel;environmentAttributes:Lnet/minecraft/world/attribute/EnvironmentAttributeSystem;", opcode = Opcodes.GETFIELD))
	private EnvironmentAttributeSystem vl$environmentAttributes(EnvironmentAttributeSystem original) {
		return vl$environmentAttributes == null ? original : vl$environmentAttributes;
	}
}
