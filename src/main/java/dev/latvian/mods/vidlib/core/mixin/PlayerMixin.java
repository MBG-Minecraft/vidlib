package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.core.VLPlayer;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.damagesource.IScalingFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements VLPlayer {
	@Shadow
	private Component displayname;

	protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
		super(type, level);
	}

	/**
	 * @author Lat
	 * @reason Optimization
	 */
	@Overwrite
	public void refreshDisplayName() {
		displayname = null;
	}

	@Inject(method = "getName", at = @At("HEAD"), cancellable = true)
	public void vl$getName(CallbackInfoReturnable<Component> cir) {
		var nickname = vl$sessionData() == null ? null : get(InternalPlayerData.NICKNAME);

		if (!Empty.isEmpty(nickname)) {
			cir.setReturnValue(nickname.copy());
		}
	}

	@Inject(method = "canHarmPlayer", at = @At("RETURN"), cancellable = true)
	public void vl$canHarmPlayer(Player other, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ() && !CommonGameEngine.INSTANCE.allowPVP(vl$self(), other)) {
			cir.setReturnValue(false);
		}
	}

	@ModifyReturnValue(method = "getSpeed", at = @At("RETURN"))
	private float vl$getSpeed(float original) {
		return original * CommonGameEngine.INSTANCE.getSpeedModifier(vl$self());
	}

	@ModifyExpressionValue(method = "getFlyingSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Abilities;getFlyingSpeed()F"))
	private float vl$getFlyingSpeed(float original) {
		return original * CommonGameEngine.INSTANCE.getFlightSpeedModifier(vl$self());
	}

	@WrapOperation(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/damagesource/IScalingFunction;scaleDamage(Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/entity/player/Player;FLnet/minecraft/world/Difficulty;)F"))
	private float vl$scaleDamage(IScalingFunction scalingFunction, DamageSource source, Player player, float damage, Difficulty difficulty, Operation<Float> operation) {
		return CommonGameEngine.INSTANCE.getScaleDamageWithDifficulty((ServerPlayer) vl$self()) ? operation.call(scalingFunction, source, player, damage, difficulty) : damage;
	}

	@Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
	private void vl$wantsToStopRiding(CallbackInfoReturnable<Boolean> cir) {
		var self = (Player) (Object) this;
		var v = self.getVehicle();

		if (v != null && v.preventDismount(self)) {
			cir.setReturnValue(false);
		}
	}

	@Override
	public Set<String> getTags() {
		if (CommonGameEngine.INSTANCE.hasImprovedPlayerTags()) {
			return get(InternalPlayerData.PLAYER_TAGS);
		} else {
			return super.getTags();
		}
	}

	@Override
	public void setTags(Collection<String> tags) {
		if (CommonGameEngine.INSTANCE.hasImprovedPlayerTags()) {
			var newSet = Set.copyOf(tags);

			if (!newSet.equals(getTags())) {
				set(InternalPlayerData.PLAYER_TAGS, newSet);
			}
		} else {
			super.setTags(tags);
		}
	}

	@Override
	public boolean addTag(String tag) {
		if (CommonGameEngine.INSTANCE.hasImprovedPlayerTags()) {
			var tags = new HashSet<>(getTags());

			if (tags.add(tag)) {
				setTags(tags);
				return true;
			} else {
				return false;
			}
		} else {
			return super.addTag(tag);
		}
	}

	@Override
	public boolean removeTag(String tag) {
		if (CommonGameEngine.INSTANCE.hasImprovedPlayerTags()) {
			var tags = new HashSet<>(getTags());

			if (tags.remove(tag)) {
				setTags(tags);
				return true;
			} else {
				return false;
			}
		} else {
			return super.removeTag(tag);
		}
	}

	@Override
	public boolean addTags(Collection<String> t) {
		if (CommonGameEngine.INSTANCE.hasImprovedPlayerTags()) {
			var tags = new HashSet<>(getTags());

			if (tags.addAll(t)) {
				setTags(tags);
				return true;
			} else {
				return false;
			}
		} else {
			return super.addTags(tags);
		}
	}

	@Override
	public boolean removeTags(Collection<String> t) {
		if (CommonGameEngine.INSTANCE.hasImprovedPlayerTags()) {
			var tags = new HashSet<>(getTags());

			if (tags.removeAll(t)) {
				setTags(tags);
				return true;
			} else {
				return false;
			}
		} else {
			return super.removeTags(tags);
		}
	}
}
