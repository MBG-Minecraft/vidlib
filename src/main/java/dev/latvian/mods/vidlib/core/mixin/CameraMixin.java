package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	private boolean initialized;

	@Shadow
	private Level level;

	@Shadow
	private Entity entity;

	@Shadow
	private boolean detached;

	@Shadow
	protected abstract void setPosition(Vec3 pos);

	@Shadow
	protected abstract void setRotation(float yaw, float pitch, float roll);

	@Shadow
	public abstract Vec3 position();

	@Shadow
	public abstract float getCameraEntityPartialTicks(DeltaTracker deltaTracker);

	@Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;alignWithEntity(F)V", shift = At.Shift.AFTER))
	private void vl$updateCameraOverride(DeltaTracker deltaTracker, CallbackInfo ci) {
		var mc = Minecraft.getInstance();
		var override = ClientGameEngine.INSTANCE.overrideCamera(mc);

		if (override != null && override.overrideCamera()) {
			var delta = getCameraEntityPartialTicks(deltaTracker);
			this.initialized = true;
			this.detached = false;
			var pos = override.getCameraPosition(delta);
			setPosition(pos);
			var rot = override.getCameraRotation(delta, pos);
			setRotation(rot.yawDeg(), rot.pitchDeg(), rot.rollDeg());
			mc.vl$applyCameraShake((Camera) (Object) this, delta);
		}
	}

	@ModifyExpressionValue(method = "update", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
	private float vl$overrideDepthFar(float original) {
		return ClientGameEngine.INSTANCE.getFarDepth(original);
	}

	@Inject(method = "update", at = @At("RETURN"))
	private void vl$updateReturn(DeltaTracker deltaTracker, CallbackInfo ci) {
		var mc = Minecraft.getInstance();

		if (this.level != null && this.entity != null && (mc.screen == null || !mc.screen.overrideCamera())) {
			var delta = getCameraEntityPartialTicks(deltaTracker);
			mc.vl$applyCameraShake((Camera) (Object) this, delta);
		}
	}


	@ModifyReturnValue(method = "isDetached", at = @At("RETURN"))
	private boolean vl$isDetached(boolean original) {
		if (!original) {
			var mc = Minecraft.getInstance();
			var override = ClientGameEngine.INSTANCE.overrideCamera(mc);
			return override != null && override.renderPlayer() && !mc.player.getBoundingBox().contains(position());
		}

		return true;
	}

	@Redirect(method = "getFluidInCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private FluidState vl$getFluidState(Level level, BlockPos pos) {
		return CommonGameEngine.INSTANCE.overrideFluidState(level, pos);
	}

	@Redirect(method = "getFluidInCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getHeight(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
	private float vl$getFluidHeight(FluidState state, BlockGetter blockGetter, BlockPos pos) {
		return blockGetter instanceof Level l ? CommonGameEngine.INSTANCE.overrideFluidHeight(l, state, pos) : state.getHeight(blockGetter, pos);
	}
}
