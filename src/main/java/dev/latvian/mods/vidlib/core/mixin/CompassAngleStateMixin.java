package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.renderer.item.properties.numeric.CompassAngleState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CompassAngleState.class)
public class CompassAngleStateMixin {
	@Shadow
	@Final
	private CompassAngleState.CompassTarget compassTarget;

	@ModifyExpressionValue(method = "getRotationTowardsCompassTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/properties/numeric/CompassAngleState;getAngleFromEntityToPos(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)D"))
	private double vl$calculate(double original, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) BlockPos target) {
		var angle = ClientGameEngine.INSTANCE.overrideCompassAngle(compassTarget, entity, target);
		return Double.isNaN(angle) ? original : angle;
	}
}
