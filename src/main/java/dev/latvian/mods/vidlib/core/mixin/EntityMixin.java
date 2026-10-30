package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.VLEntity;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin implements VLEntity {
	/**
	 * 1.21.5 hooked {@code Entity.collectColliders}. 26.3 split that into
	 * {@code collectAllColliders(Entity, Level, AABB)} (plus a
	 * {@code collectCollidersIgnoringWorldBorder} overload that takes a
	 * {@code CollisionContext}); the VidL zone shapes are still looked up by the injected
	 * {@code Level.vl$getShapesIntersecting}.
	 */
	@ModifyReturnValue(method = "collectAllColliders", at = @At("RETURN"))
	private static List<VoxelShape> vl$collectColliders(List<VoxelShape> parent, @Local(argsOnly = true) @org.jetbrains.annotations.Nullable Entity entity, @Local(argsOnly = true) Level level, @Local(argsOnly = true) AABB collisionBox) {
		var list = level.vl$getShapesIntersecting(entity, collisionBox);

		if (parent.isEmpty()) {
			return list;
		} else if (!list.isEmpty()) {
			list.addAll(0, parent);
			return list;
		} else {
			return parent;
		}
	}

	@Inject(method = "getAddEntityPacket", at = @At("HEAD"), cancellable = true)
	private void vl$getAddEntityPacket(ServerEntity serverEntity, CallbackInfoReturnable<Packet<ClientGamePacketListener>> cir) {
		var override = CommonGameEngine.INSTANCE.overrideEntitySpawnPacket(vl$self(), serverEntity);

		if (override != null) {
			cir.setReturnValue(override);
		}
	}

	@ModifyReturnValue(method = "getGravity", at = @At("RETURN"))
	private double vl$getGravity(double original) {
		return original * CommonGameEngine.INSTANCE.getGravityModifier(vl$self());
	}

	/**
	 * @author Lat
	 * @reason Fix forever falling
	 */
	@Overwrite
	protected void applyGravity() {
		var entity = (Entity) (Object) this;
		double gravity = entity.getGravity();
		var delta = entity.getDeltaMovement();

		if (gravity != 0D) {
			entity.setDeltaMovement(delta.add(0D, -gravity, 0D));
		} else {
			entity.setDeltaMovement(new Vec3(delta.x, 0D, delta.z));
			entity.resetFallDistance();
		}
	}

	/**
	 * 1.21.5 hooked {@code Entity.updateFluidOnEyes} / {@code updateFluidHeightAndDoFluidPushing}.
	 * 26.3 moved the whole per-fluid block scan into {@code EntityFluidInteraction.update(...)}
	 * (a separate class whose level is only a {@link BlockGetter}), so the fluid overrides now
	 * live in {@link EntityFluidInteractionMixin}; the remaining logic is untouched.
	 */

	@Inject(method = "addPassenger", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;"))
	private void vl$addPassenger(Entity passenger, CallbackInfo ci, @Local List<Entity> list) {
		sortPassengers(list);
	}
}
