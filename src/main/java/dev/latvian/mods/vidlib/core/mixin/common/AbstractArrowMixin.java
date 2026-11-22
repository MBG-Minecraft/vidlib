package dev.latvian.mods.vidlib.core.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {
	@Shadow
	private int life;

	@Unique
	private Vec3 vl$prevTrailPos;

	@Inject(method = "tick", at = @At("RETURN"))
	private void vl$tick(CallbackInfo ci) {
		var arrow = (AbstractArrow) (Object) this;

		if (life == 0 && !arrow.level().isClientSide() && VLBridge.getArrowTrail.test(arrow)) {
			var pos = new Vec3(arrow.xo, arrow.yo, arrow.zo);

			if (vl$prevTrailPos != null) {
				VLBridge.spawnArrowTrail.apply(arrow, vl$prevTrailPos, pos);
			}

			vl$prevTrailPos = pos;
		}
	}

	@Inject(method = "getPickupItem", at = @At("HEAD"), cancellable = true)
	private void vl$getPickupItem(CallbackInfoReturnable<ItemStack> cir) {
		if (!VLBridge.getInfiniteArrows.get().isEmpty()) {
			cir.setReturnValue(ItemStack.EMPTY);
		}
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
	private void vl$critParticle(Level instance, ParticleOptions options, double x, double y, double z, double dx, double dy, double dz, Operation<Void> original) {
		if (!VLBridge.getArrowTrail.test((AbstractArrow) (Object) this)) {
			original.call(instance, options, x, y, z, dx, dy, dz);
		}
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
	private VoxelShape vl$getShape(BlockState instance, BlockGetter blockGetter, BlockPos pos, Operation<VoxelShape> original) {
		return instance.getCollisionShape(blockGetter, pos, CollisionContext.of((AbstractArrow) (Object) this));
	}
}
