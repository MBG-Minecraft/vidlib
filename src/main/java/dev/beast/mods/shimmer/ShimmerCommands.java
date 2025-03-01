package dev.beast.mods.shimmer;

import com.mojang.brigadier.CommandDispatcher;
import dev.beast.mods.shimmer.content.clock.ClockContent;
import dev.beast.mods.shimmer.feature.camerashake.CameraShakeCommands;
import dev.beast.mods.shimmer.feature.cutscene.CutsceneCommands;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;

public class ShimmerCommands {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
		dispatcher.register(Commands.literal("shimmer")
			.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
			.then(ClockContent.createCommand())
			.then(CutsceneCommands.createCommand(buildContext))
			.then(CameraShakeCommands.createCommand(buildContext))
			.then(Commands.literal("set-fake-block")
				.then(Commands.argument("pos", BlockPosArgument.blockPos())
					.then(Commands.argument("state", BlockStateArgument.block(buildContext))
						.executes(ctx -> {
							var pos = BlockPosArgument.getBlockPos(ctx, "pos");
							var state = BlockStateArgument.getBlock(ctx, "state").getState();
							ctx.getSource().getLevel().setFakeBlock(pos, state);
							return 1;
						})
					)
				)
			)
		);
	}
}
