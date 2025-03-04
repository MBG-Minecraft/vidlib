package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.ShimmerConfig;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(StructureBlockEntity.class)
public class StructureBlockEntityMixin {
	@ModifyConstant(method = "loadAdditional", constant = {@Constant(intValue = 48), @Constant(intValue = -48)})
	private int shimmer$loadAdditional(int value) {
		return value > 0 ? ShimmerConfig.structureBlockRange + 1 : -ShimmerConfig.structureBlockRange;
	}

	@ModifyConstant(method = "detectSize", constant = @Constant(intValue = 80))
	private int shimmer$detectSize(int value) {
		return ShimmerConfig.structureBlockRange;
	}
}
