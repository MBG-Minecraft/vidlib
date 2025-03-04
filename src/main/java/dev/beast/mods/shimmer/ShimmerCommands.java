package dev.beast.mods.shimmer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.beast.mods.shimmer.feature.camerashake.CameraShakeCommands;
import dev.beast.mods.shimmer.feature.clock.ClockCommands;
import dev.beast.mods.shimmer.feature.cutscene.CutsceneCommands;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class ShimmerCommands {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
		dispatcher.register(Commands.literal("shimmer")
			.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
			.then(ClockCommands.createCommand())
			.then(CutsceneCommands.createCommand(buildContext))
			.then(CameraShakeCommands.createCommand(buildContext))
			.then(Commands.literal("set-fake-block")
				.then(Commands.argument("pos", BlockPosArgument.blockPos())
					.then(Commands.argument("state", BlockStateArgument.block(buildContext))
						.executes(ctx -> setFakeBlock(ctx.getSource(), BlockPosArgument.getBlockPos(ctx, "pos"), BlockStateArgument.getBlock(ctx, "state").getState()))
					)
				)
			)
			.then(Commands.literal("post-effect")
				.then(Commands.argument("id", ResourceLocationArgument.id())
					.executes(ctx -> setPostEffect(ctx.getSource(), ResourceLocationArgument.getId(ctx, "id")))
				)
			)
		);
	}

	private static int setFakeBlock(CommandSourceStack source, BlockPos pos, BlockState state) {
		source.getLevel().setFakeBlock(pos, state);
		return 1;
	}

	private static int setPostEffect(CommandSourceStack source, ResourceLocation id) throws CommandSyntaxException {
		source.getPlayerOrException().setPostEffect(id);
		return 1;
	}
}
