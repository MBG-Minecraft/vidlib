package dev.beast.mods.shimmer.feature.misc.command;

import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface SetFakeBlockCommand {
	@AutoRegister
	ServerCommandHolder HOLDER = new ServerCommandHolder("set-fake-block", (command, buildContext) -> command
		.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
		.then(Commands.argument("pos", BlockPosArgument.blockPos())
			.then(Commands.argument("state", BlockStateArgument.block(buildContext))
				.executes(ctx -> setFakeBlock(ctx.getSource(), BlockPosArgument.getBlockPos(ctx, "pos"), BlockStateArgument.getBlock(ctx, "state").getState()))
			)
		)
	);

	private static int setFakeBlock(CommandSourceStack source, BlockPos pos, BlockState state) {
		source.getLevel().setFakeBlock(pos, state);
		return 1;
	}
}
