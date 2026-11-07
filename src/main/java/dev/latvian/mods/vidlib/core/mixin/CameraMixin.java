package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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

	/**
	 * 1.21.5 hooked {@code setup(BlockGetter, Entity, boolean, boolean, float)}. 26.3 split the
	 * level/entity/detached inputs into their own fields and condensed it to
	 * {@code update(DeltaTracker)}, with the partial tick coming from the delta tracker.
	 */
	@Inject(method = "update", at = @At("HEAD"), cancellable = true)
	private void vl$setupHead(net.minecraft.client.DeltaTracker deltaTracker, CallbackInfo ci) {
		var mc = Minecraft.getInstance();
		var override = ClientGameEngine.INSTANCE.overrideCamera(mc);

		if (override != null && override.overrideCamera()) {
			this.initialized = true;
			this.detached = false;
			float delta = deltaTracker.getGameTimeDeltaPartialTick(false);
			var pos = override.getCameraPosition(delta);
			setPosition(pos);
			var rot = override.getCameraRotation(delta, pos);
			setRotation(rot.yawDeg(), rot.pitchDeg(), rot.rollDeg());
			mc.vl$applyCameraShake((Camera) (Object) this, delta);
			ci.cancel();
		}
	}

	@Inject(method = "update", at = @At("RETURN"))
	private void vl$setupReturn(net.minecraft.client.DeltaTracker deltaTracker, CallbackInfo ci) {
		var mc = Minecraft.getInstance();

		if (mc.gui.screen() == null || !mc.gui.screen().overrideCamera()) {
			mc.vl$applyCameraShake((Camera) (Object) this, deltaTracker.getGameTimeDeltaPartialTick(false));
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

	@ModifyVariable(method = "update", at = @At("STORE"), ordinal = 0)
	private float vl$renderDistance(float renderDistance) {
		return ClientGameEngine.INSTANCE.getFarDepth(renderDistance);
	}

	@Redirect(method = "getFluidInCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private FluidState vl$getFluidState(Level level, BlockPos pos) {
		return CommonGameEngine.INSTANCE.overrideFluidState(level, pos);
	}
}
