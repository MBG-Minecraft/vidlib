package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;

public interface AnchorCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("anchor", (command, buildContext) -> command
		.requires(source -> Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source))
		.then(Commands.literal("set")
			.then(Commands.argument("start", BlockPosArgument.blockPos())
				.then(Commands.argument("end", BlockPosArgument.blockPos())
					.executes(ctx -> set(ctx.getSource(), BlockPosArgument.getBlockPos(ctx, "start"), BlockPosArgument.getBlockPos(ctx, "end")))
				)
			)
		)
		.then(Commands.literal("remove")
			.executes(ctx -> remove(ctx.getSource()))
		)
	);

	private static int set(CommandSourceStack source, BlockPos start, BlockPos end) {
		var session = LevelSessionData.of(source.getLevel());
		session.setAnchor(Anchor.create(start, end));
		return 1;
	}

	private static int remove(CommandSourceStack source) {
		var session = LevelSessionData.of(source.getLevel());
		session.setAnchor(Anchor.NONE);
		return 1;
	}
}
