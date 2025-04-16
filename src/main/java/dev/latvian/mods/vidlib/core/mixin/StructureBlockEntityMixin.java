package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.VidLibConfig;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(StructureBlockEntity.class)
public class StructureBlockEntityMixin {
	@ModifyConstant(method = "loadAdditional", constant = {@Constant(intValue = 48), @Constant(intValue = -48)})
	private int vl$loadAdditional(int value) {
		return value > 0 ? VidLibConfig.structureBlockRange + 1 : -VidLibConfig.structureBlockRange;
	}

	@ModifyConstant(method = "detectSize", constant = @Constant(intValue = 80))
	private int vl$detectSize(int value) {
		return VidLibConfig.structureBlockRange;
	}
}
