package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.LodestoneTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DataComponents.class)
public class DataComponentsMixin {
	@ModifyConstant(method = "lambda$static$1", constant = @Constant(intValue = 99))
	private static int vl$maxSlotSize(int original) {
		return 1_000_000_000;
	}

	@WrapOperation(method = "lambda$static$58", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/DataComponentType$Builder;persistent(Lcom/mojang/serialization/Codec;)Lnet/minecraft/core/component/DataComponentType$Builder;"))
	private static DataComponentType.Builder<LodestoneTracker> vl$lodestone(DataComponentType.Builder<LodestoneTracker> instance, Codec<LodestoneTracker> codec, Operation<DataComponentType.Builder<LodestoneTracker>> original) {
		return original.call(instance, codec);
	}
}
