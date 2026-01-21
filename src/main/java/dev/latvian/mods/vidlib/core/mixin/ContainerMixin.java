package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Container.class)
public interface ContainerMixin {
	@ModifyConstant(method = "getMaxStackSize()I", constant = @Constant(intValue = 99))
	private int vl$maxSlotSize(int original) {
		return 1_000_000_000;
	}
}
