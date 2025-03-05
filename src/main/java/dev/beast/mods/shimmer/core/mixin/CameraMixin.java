package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerCamera;
import dev.beast.mods.shimmer.feature.misc.CameraOverride;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin implements ShimmerCamera {
	@Shadow
	private boolean initialized;

	@Shadow
	private BlockGetter level;

	@Shadow
	private Entity entity;

	@Shadow
	private boolean detached;

	@Shadow
	protected abstract void setPosition(Vec3 pos);

	@Shadow
	protected abstract void setRotation(float yaw, float pitch, float roll);

	@Override
	@Invoker("setPosition")
	public abstract void shimmer$setPosition(Vec3 pos);

	@Inject(method = "setup", at = @At("HEAD"), cancellable = true)
	private void shimmer$setupHead(BlockGetter area, Entity entity, boolean detached, boolean inverseView, float delta, CallbackInfo ci) {
		var mc = Minecraft.getInstance();
		var override = CameraOverride.get(mc);

		if (override != null && override.overrideCamera()) {
			this.initialized = true;
			this.level = area;
			this.entity = entity;
			this.detached = false;
			var pos = override.getCameraPosition(delta);
			setPosition(pos);
			var rot = override.getCameraRotation(delta, pos);
			setRotation(rot.x, rot.y, rot.z);
			mc.shimmer$applyCameraShake((Camera) (Object) this, delta);
			ci.cancel();
		}
	}

	@Inject(method = "setup", at = @At("RETURN"))
	private void shimmer$setupReturn(BlockGetter area, Entity entity, boolean detached, boolean inverseView, float delta, CallbackInfo ci) {
		var mc = Minecraft.getInstance();

		if (mc.screen == null || !mc.screen.overrideCamera()) {
			mc.shimmer$applyCameraShake((Camera) (Object) this, delta);
		}
	}
}
