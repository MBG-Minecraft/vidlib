package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.VLEntity;
import dev.latvian.mods.vidlib.feature.entity.EntityOverride;
import dev.latvian.mods.vidlib.feature.entity.EntityOverrideValue;
import dev.latvian.mods.vidlib.feature.entity.ExactEntitySpawnPayload;
import dev.latvian.mods.vidlib.feature.input.PlayerInput;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(Entity.class)
public abstract class EntityMixin implements VLEntity {
	@Shadow
	private Level level;

	@Shadow
	public abstract void load(CompoundTag compound);

	@Shadow
	public abstract boolean removeTag(String tag);

	@Unique
	private PlayerInput vl$pilotInput = PlayerInput.NONE;

	@ModifyReturnValue(method = "collectColliders", at = @At("RETURN"))
	private static List<VoxelShape> vl$collectColliders(List<VoxelShape> parent, @Local(argsOnly = true) Level level, @Local(argsOnly = true) @Nullable Entity entity, @Local(argsOnly = true) AABB collisionBox) {
		var zones = level.vl$getActiveZones();

		if (zones != null) {
			var list = zones.getShapesIntersecting(entity, collisionBox);

			if (parent.isEmpty()) {
				return list;
			} else if (!list.isEmpty()) {
				var list2 = new ArrayList<VoxelShape>(parent.size() + list.size());
				list2.addAll(parent);
				list2.addAll(list);
				return list2;
			}
		}

		return parent;
	}

	@Unique
	private Map<EntityOverride<?>, EntityOverrideValue<?>> vl$entityOverridesMap;

	@Unique
	private boolean vl$isSaving = false;

	@Nullable
	@Override
	public Map<EntityOverride<?>, EntityOverrideValue<?>> vl$getEntityOverridesMap() {
		return vl$entityOverridesMap;
	}

	@Override
	public void vl$setEntityOverridesMap(@Nullable Map<EntityOverride<?>, EntityOverrideValue<?>> map) {
		vl$entityOverridesMap = map;
	}

	@Override
	public boolean vl$isSaving() {
		return vl$isSaving;
	}

	@Inject(method = "getAddEntityPacket", at = @At("HEAD"), cancellable = true)
	private void vl$getAddEntityPacket(ServerEntity serverEntity, CallbackInfoReturnable<Packet<ClientGamePacketListener>> cir) {
		var e = (Entity) (Object) this;

		if (!e.getType().builtInRegistryHolder().getKey().location().getNamespace().equals("minecraft")) {
			cir.setReturnValue((Packet) new ExactEntitySpawnPayload(e, serverEntity, 0).toS2C(e.level()));
		}
	}

	@Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
	private void vl$isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
		var override = level == null || !level.isClientSide ? null : vl$glowingOverride();

		if (override != null) {
			cir.setReturnValue(override);
		}
	}

	@Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
	private void vl$getTeamColor(CallbackInfoReturnable<Integer> cir) {
		var override = vl$teamColorOverride();

		if (override != null) {
			cir.setReturnValue(override);
		}
	}

	@ModifyReturnValue(method = "getGravity", at = @At("RETURN"))
	private double vl$getGravity(double original) {
		return original * vl$gravityMod();
	}

	@Inject(method = "saveWithoutId", at = @At("HEAD"))
	private void vl$beforeSave(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
		vl$isSaving = true;
	}

	@Inject(method = "saveWithoutId", at = @At("RETURN"))
	private void vl$afterSave(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
		vl$isSaving = false;
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

	@Override
	public PlayerInput getPilotInput() {
		return vl$pilotInput;
	}

	@Override
	public void vl$setPilotInput(PlayerInput input) {
		vl$pilotInput = input;
	}

	@Inject(method = "removePassenger", at = @At("RETURN"))
	private void vl$removePassenger(Entity passenger, CallbackInfo ci) {
		vl$pilotInput = PlayerInput.NONE;
	}

	@Redirect(method = {"updateFluidOnEyes", "updateFluidHeightAndDoFluidPushing()V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
	private FluidState vl$getFluidState(Level level, BlockPos pos) {
		return level.vl$overrideFluidState(pos);
	}

	@Redirect(method = {"updateFluidOnEyes", "updateFluidHeightAndDoFluidPushing()V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;getHeight(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
	private float vl$getFluidHeight(FluidState state, BlockGetter blockGetter, BlockPos pos) {
		return blockGetter instanceof Level l ? l.vl$overrideFluidHeight(state, pos) : state.getHeight(blockGetter, pos);
	}
}
