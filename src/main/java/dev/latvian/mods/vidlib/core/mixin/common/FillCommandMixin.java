package dev.latvian.mods.vidlib.core.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.FillCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FillCommand.class)
public class FillCommandMixin {
	@WrapOperation(method = {
		"lambda$register$2",
		"lambda$register$3",
		"lambda$register$4",
		"lambda$register$5",
		"lambda$register$7",
		"lambda$register$8",
		"lambda$register$9",
	}, at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/coordinates/BlockPosArgument;getLoadedBlockPos(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/String;)Lnet/minecraft/core/BlockPos;"))
	private static BlockPos vl$pos(CommandContext<CommandSourceStack> context, String name, Operation<BlockPos> original) {
		return BlockPosArgument.getBlockPos(context, name);
	}

	@ModifyExpressionValue(method = "fillBlocks", at = @At(value = "INVOKE", target = "Ljava/lang/Integer;intValue()I"))
	private static int vl$expand(int original) {
		return Integer.MAX_VALUE;
	}
}
