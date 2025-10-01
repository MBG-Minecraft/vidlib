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

public interface StructureCommands {
	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("structure", (command, buildContext) -> command
		.requires(source -> source.hasPermission(2))
		.then(Commands.literal("add-blocks")
			.then(Commands.argument("start", BlockPosArgument.blockPos())
				.then(Commands.argument("end", BlockPosArgument.blockPos())
					.executes(ctx -> {
						var start = BlockPosArgument.getBlockPos(ctx, "start");
						var end = BlockPosArgument.getBlockPos(ctx, "end");
						StructureCapture.CURRENT.getValue().addBlocks(ctx.getSource().getLevel(), ctx.getSource(), start, end);
						return 1;
					})
				)
			)
		)
		.then(Commands.literal("reset")
			.executes(ctx -> {
				ctx.getSource().sendSuccess(() -> Component.literal("Removed %,d blocks".formatted(StructureCapture.CURRENT.getValue().blocks.size())), false);
				StructureCapture.CURRENT.setValue(new CurrentStructureCapture());
				return 1;
			})
		)
		.then(Commands.literal("set-ignore-filter")
			.then(Commands.argument("filter", BlockFilter.COMMAND.argument(buildContext))
				.executes(ctx -> {
					StructureCapture.IGNORE_FILTER.setValue(BlockFilter.COMMAND.get(ctx, "filter"));
					return 1;
				})
			)
		)
		.then(Commands.literal("fluids")
			.then(Commands.argument("fluids", BoolArgumentType.bool())
				.executes(ctx -> {
					StructureCapture.INCLUDE_FLUIDS.set(BoolArgumentType.getBool(ctx, "fluids"));
					return 1;
				})
			)
		)
		.then(Commands.literal("particles")
			.then(Commands.argument("particles", BoolArgumentType.bool())
				.executes(ctx -> {
					StructureCapture.PARTICLES.set(BoolArgumentType.getBool(ctx, "particles"));
					return 1;
				})
			)
		)
		.then(Commands.literal("save")
			.then(Commands.literal("ghost-chunks")
				.then(Commands.argument("name", StringArgumentType.word())
					.executes(ctx -> StructureCapture.capture(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT)))
				)
			)
			.then(Commands.literal("full")
				.then(Commands.argument("name", StringArgumentType.word())
					.executes(ctx -> StructureCapture.save(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT), false))
				)
			)
			.then(Commands.literal("shell")
				.then(Commands.argument("name", StringArgumentType.word())
					.executes(ctx -> StructureCapture.save(ctx.getSource(), StringArgumentType.getString(ctx, "name").toLowerCase(Locale.ROOT), true))
				)
			)
		)
		.then(Commands.literal("move")
			.then(Commands.argument("to", BlockPosArgument.blockPos())
				.executes(ctx -> StructureCapture.move(BlockPosArgument.getBlockPos(ctx, "to")))
			)
		)
	);
}
