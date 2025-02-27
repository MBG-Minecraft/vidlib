package dev.beast.mods.shimmer.core.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@ModifyConstant(
		method = "onScroll",
		constant = @Constant(floatValue = 0.2f)
	)
	private float shimmer$maxFlySpeed(float constant) {
		return 3f;
	}
}
