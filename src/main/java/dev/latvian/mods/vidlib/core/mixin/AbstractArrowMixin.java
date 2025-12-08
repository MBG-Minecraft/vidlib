package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.gradient.ClientGradients;
import dev.latvian.mods.vidlib.feature.particle.LineParticleOptions;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
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
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {
	@Shadow
	private int life;

	@Unique
	private Vec3 vl$prevTrailPos;

	@ModifyConstant(method = "tickDespawn", constant = @Constant(intValue = 1200))
	private int vl$despawnLife(int o) {
		return CommonGameEngine.INSTANCE.getArrowDespawnTime((AbstractArrow) (Object) this);
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private void vl$tick(CallbackInfo ci) {
		var arrow = (AbstractArrow) (Object) this;

		if (life == 0 && !arrow.level().isClientSide() && CommonGameEngine.INSTANCE.getArrowTrail(arrow)) {
			var pos = arrow.getPosition(0F);

			if (vl$prevTrailPos != null) {
				var delta = vl$prevTrailPos.subtract(pos);

				if (delta.lengthSqr() > 0.0001D) {
					((ServerLevel) arrow.level()).sendParticles(
						new LineParticleOptions(20, ClientGradients.TRAIL, ClientGradients.TRAIL, 1),
						true,
						true,
						pos.x,
						pos.y,
						pos.z,
						0,
						delta.x,
						delta.y,
						delta.z,
						1D
					);
				}
			}

			vl$prevTrailPos = pos;
		}
	}

	@Inject(method = "getPickupItem", at = @At("HEAD"), cancellable = true)
	private void vl$getPickupItem(CallbackInfoReturnable<ItemStack> cir) {
		if (CommonGameEngine.INSTANCE.getInfiniteArrows()) {
			cir.setReturnValue(ItemStack.EMPTY);
		}
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
	private void vl$critParticle(Level instance, ParticleOptions options, double x, double y, double z, double dx, double dy, double dz) {
		if (!CommonGameEngine.INSTANCE.getArrowTrail((AbstractArrow) (Object) this)) {
			instance.addParticle(options, x, y, z, dx, dy, dz);
		}
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
	private VoxelShape vl$getShape(BlockState instance, BlockGetter blockGetter, BlockPos pos) {
		return instance.getCollisionShape(blockGetter, pos, CollisionContext.of((AbstractArrow) (Object) this));
	}
}
