package dev.latvian.mods.vidlib.core.mixin.neoforge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.damagesource.IScalingFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerScaleDamageMixin {
	@WrapOperation(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/damagesource/IScalingFunction;scaleDamage(Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/entity/player/Player;FLnet/minecraft/world/Difficulty;)F"))
	private float vl$scaleDamage(IScalingFunction scalingFunction, DamageSource source, Player player, float damage, Difficulty difficulty, Operation<Float> operation) {
		return CommonGameEngine.INSTANCE.getScaleDamageWithDifficulty((ServerPlayer) (Object) this) ? operation.call(scalingFunction, source, player, damage, difficulty) : damage;
	}
}
