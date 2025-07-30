package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public interface GhostChunkCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("ghost-chunks", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("add-blocks")
			.then(Commands.argument("start", BlockPosArgument.blockPos())
				.then(Commands.argument("end", BlockPosArgument.blockPos())
					.executes(ctx -> {
						var start = BlockPosArgument.getBlockPos(ctx, "start");
						var end = BlockPosArgument.getBlockPos(ctx, "end");
						GhostStructureCapture.CURRENT.getValue().addBlocks(ctx.getSource().getLevel(), ctx.getSource(), start, end);
						return 1;
					})
				)
			)
		)
		.then(Commands.literal("reset")
			.executes(ctx -> {
				ctx.getSource().sendSuccess(() -> Component.literal("Removed %,d blocks".formatted(GhostStructureCapture.CURRENT.getValue().blocks.size())), false);
				GhostStructureCapture.CURRENT.setValue(new CurrentGhostStructureCapture());
				return 1;
			})
		)
		.then(Commands.literal("set-ignore-filter")
			.then(Commands.argument("filter", BlockFilter.COMMAND.argument(buildContext))
				.executes(ctx -> {
					GhostStructureCapture.IGNORE_FILTER.setValue(BlockFilter.COMMAND.get(ctx, "filter"));
					return 1;
				})
			)
		)
		.then(Commands.literal("fluids")
			.then(Commands.argument("fluids", BoolArgumentType.bool())
				.executes(ctx -> {
					GhostStructureCapture.INCLUDE_FLUIDS.set(BoolArgumentType.getBool(ctx, "fluids"));
					return 1;
				})
			)
		)
		.then(Commands.literal("particles")
			.then(Commands.argument("particles", BoolArgumentType.bool())
				.executes(ctx -> {
					GhostStructureCapture.PARTICLES.set(BoolArgumentType.getBool(ctx, "particles"));
					return 1;
				})
			)
		)
		.then(Commands.literal("capture")
			.then(Commands.argument("name", StringArgumentType.word())
				.executes(ctx -> GhostStructureCapture.capture(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT)))
			)
		)
		.then(Commands.literal("save")
			.then(Commands.argument("name", StringArgumentType.word())
				.executes(ctx -> GhostStructureCapture.save(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT), false))
				.then(Commands.argument("shell", BoolArgumentType.bool())
					.executes(ctx -> GhostStructureCapture.save(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT), BoolArgumentType.getBool(ctx, "shell")))
				)
			)
		)
		.then(Commands.literal("move")
			.then(Commands.argument("to", BlockPosArgument.blockPos())
				.executes(ctx -> GhostStructureCapture.move(BlockPosArgument.getBlockPos(ctx, "to")))
			)
		)
	);
}
