package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.world.level.block.MushroomBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MushroomBlock.class)
public class MushroomBlockMixin {
	@ModifyConstant(method = "canSurvive", constant = @Constant(intValue = 13))
	private int vl$allowedLight(int original) {
		return 16;
	}
}
