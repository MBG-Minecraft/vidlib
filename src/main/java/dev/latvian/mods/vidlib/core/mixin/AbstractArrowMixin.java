package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.VidLibConfig;
import dev.latvian.mods.vidlib.feature.gradient.ClientGradients;
import dev.latvian.mods.vidlib.feature.particle.LineParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.projectile.AbstractArrow;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {
	@Shadow
	private int life;

	@Unique
	private Vec3 vl$prevTrailPos;

	@Inject(method = "tickDespawn", at = @At("RETURN"))
	private void vl$tickDespawn(CallbackInfo ci) {
		if (life >= 20 && VidLibConfig.fastArrowDespawn) {
			((AbstractArrow) (Object) this).discard();
		}
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private void vl$tick(CallbackInfo ci) {
		var arrow = (AbstractArrow) (Object) this;

		if (life == 0 && VidLibConfig.arrowTrails && arrow.level().isClientSide()) {
			if (vl$prevTrailPos != null) {
				var pos = arrow.position();
				var delta = vl$prevTrailPos.subtract(pos);

				if (delta.lengthSqr() > 0.0001D) {
					arrow.level().addParticle(new LineParticleOptions(20, ClientGradients.TRAIL, ClientGradients.TRAIL, 1), true, true, pos.x, pos.y, pos.z, delta.x, delta.y, delta.z);
				}

				vl$prevTrailPos = pos;
			}

			vl$prevTrailPos = arrow.position();
		}
	}

	@Inject(method = "getPickupItem", at = @At("HEAD"), cancellable = true)
	private void vl$getPickupItem(CallbackInfoReturnable<ItemStack> cir) {
		if (VidLibConfig.infiniteArrows) {
			cir.setReturnValue(ItemStack.EMPTY);
		}
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
	private void vl$critParticle(Level instance, ParticleOptions options, double x, double y, double z, double dx, double dy, double dz) {
		if (!VidLibConfig.arrowTrails) {
			instance.addParticle(options, x, y, z, dx, dy, dz);
		}
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
	private VoxelShape vl$tick(BlockState instance, BlockGetter blockGetter, BlockPos pos) {
		return instance.getCollisionShape(blockGetter, pos, CollisionContext.of((AbstractArrow) (Object) this));
	}
}
